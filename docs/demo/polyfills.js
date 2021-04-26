
// HACK(Richo): trim polyfill
if (!String.prototype.trim) {
  (function() {
    // Make sure we trim BOM and NBSP
    let rtrim = /^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g;
    String.prototype.trim = function() {
      return this.replace(rtrim, '');
    };
  })();
}

// HACK(Richo): flat polyfill
if (!Array.prototype.flat) {
  Object.defineProperty(Array.prototype, 'flat', {
    value: function(depth = 1) {
      return this.reduce(function (flat, toFlatten) {
        return flat.concat((Array.isArray(toFlatten) && (depth>1)) ? toFlatten.flat(depth-1) : toFlatten);
      }, []);
    }
  });
}
