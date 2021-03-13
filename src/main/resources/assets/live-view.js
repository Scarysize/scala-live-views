const eventSource = new EventSource("/live-demo/updates");

eventSource.addEventListener('error', err => console.error(err));
eventSource.addEventListener('open', () => console.log("update connected"));
eventSource.addEventListener('change', event => {
    const changes = JSON.parse(event.data);
    apply(changes, document.documentElement);
});


function apply(changes, root) {
    console.log('applying changes...');

    for (const change of changes) {
        console.log(change);
        const node = findChildAtIndex(root, change.path);
        patchText(change, node.node);
    }

}

function patchText(change, textNode) {
    const oldString = textNode.textContent;
    let newString = '';

    let pointer = 0;
    for (const patch of change.patch) {
        if (patch.operation === 'EQUAL') {
            newString += oldString.substr(pointer, patch.text.length);
            pointer += patch.text.length;
        } else if (patch.operation === 'DELETE') {
            pointer += patch.text.length;
        } else if (patch.operation === 'INSERT') {
            newString += patch.text;
            // pointer += patch.text.length;
        }
    }

    textNode.textContent = newString;
}

function findChildAtIndex(root, path) {
    if (!path || !root.childNodes || root.childNodes.length === 0) {
        return null;
    }

    const indices = path.split('>');
    let found = true;
    let lastParentIndex = '';
    for (let i = 1; i < indices.length; i++) {
        const nodeIndex = parseInt(indices[i], 10);
        if (root.childNodes && root.childNodes.length > nodeIndex) {
            root = root.childNodes[nodeIndex];
        } else {
            lastParentIndex = indices.slice(0, i - 1).join('>');
            found = false;
            break;
        }
    }

    return {
        lastParent: found ? root.parentNode : root,
        lastParentIndex: found ? path.slice(0, path.lastIndexOf('>')) : lastParentIndex,
        node: found ? root : null,
        found: found
    };
}
