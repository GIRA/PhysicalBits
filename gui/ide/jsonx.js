let JSONX = (function () {

  /*
  HACK(Richo): This function will fix occurrences of Infinity, -Infinity, and NaN
  in the JSON object resulting from a server response. Since JSON	doesn't handle
  these values correctly I'm encoding them in a special way.
  */
  function fixInvalidJSONFloats(obj) {
    if (obj instanceof Array) return obj.map(fixInvalidJSONFloats);
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
      value[m] = fixInvalidJSONFloats(obj[m]);
    }
    return value;
  }

  function reviveSpecialFloats(key, value) {
    if (typeof value != "object") return value;
    if (value === null) return null;
    if (value === undefined) return undefined;

    if (value["___INF___"] !== undefined) {
      return Infinity * value["___INF___"];
    } else if (value["___NAN___"] !== undefined) {
      return NaN;
    }
    return value;
  }

  return {
    parse1: str => fixInvalidJSONFloats(JSON.parse(str)),
    parse2: str => JSON.parse(str, reviveSpecialFloats),
  }

})();
