const eventSource = new EventSource("/live-demo/updates");

eventSource.addEventListener('error', err => console.error(err));
eventSource.addEventListener('open', () => console.log("update connected"));
eventSource.addEventListener('change', event => {
    const changes = JSON.parse(event.data);
    apply(changes, document.documentElement);
});


function apply(changes, base) {
    console.log('applying changes...');

    for (const change of changes) {
        console.log(change);
        const node = findChildAtIndex(base, change.baseIndex);
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

function findChildAtIndex(node, index) {
    if (!index || !node.childNodes || node.childNodes.length === 0) {
        return null;
    }

    const indices = index.split('>');
    let found = true;
    let lastParentIndex = '';
    for (let i = 1; i < indices.length; i++) {
        const nodeIndex = parseInt(indices[i], 10);
        if (node.childNodes && node.childNodes.length > nodeIndex) {
            node = node.childNodes[nodeIndex];
        } else {
            lastParentIndex = indices.slice(0, i - 1).join('>');
            found = false;
            break;
        }
    }

    return {
        lastParent: found ? node.parentNode : node,
        lastParentIndex: found ? index.slice(0, index.lastIndexOf('>')) : lastParentIndex,
        node: found ? node : null,
        found: found
    };
}
