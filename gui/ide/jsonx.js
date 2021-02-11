let JSONX = (function () {

  /*
  HACK(Richo): This function will fix occurrences of Infinity, -Infinity, and NaN
  in the JSON object resulting from a server response. Since JSON	doesn't handle
  these values correctly I'm encoding them in a special way.
  */
  function decodeSpecialFloats(obj) {
    if (obj instanceof Array) return obj.map(decodeSpecialFloats);
    if (typeof obj != "object") return obj;
    if (obj === null) return null;
    if (obj === undefined) return undefined;

    if (obj["___INF___"] !== undefined) {
      return Infinity * obj["___INF___"];
    } else if (obj["___NAN___"] !== undefined) {
      return NaN;
    }

    let value = {};
    for (let m in obj) {
      value[m] = decodeSpecialFloats(obj[m]);
    }
    return value;
  }

  return {
    parse: str => decodeSpecialFloats(JSON.parse(str)),
  }

})();
