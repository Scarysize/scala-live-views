# scala-live-views

Server rendered stateful views where only a stream of DOM patches is sent to the client. State and its mutation are done on the server.

## Basis
- Akka http, actors, streams
- Scalatags
- Google's "diff-match-patch"

## Inspiration
- [reconcile.js](https://github.com/nyxtom/reconcile.js)
- react.js
- phoenix live-view (elixir)

## Status

### Reconciliation
- [x] Diffs for text content 
- [x] Diffs for node moves
- [ ] Diffs for node attributes
- [ ] Diffs for styles
- [ ] Diffs for css classes

### State
- [ ] Persistent client identification (cookie?)
- [ ] Per client change stream
- [ ] Per client state

### Protocol
- [ ] Client events
- [ ] Conflict handling/synchronisation issues

### Other
- [ ] Cool demo

All right reserved.
