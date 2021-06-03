
/*
HACK(Richo): This object will fix occurrences of Infinity, -Infinity, and NaN
in the JSON objects resulting from a parse/stringify. Since JSON	doesn't handle
these values correctly I'm encoding them in a special way:

    Infinity    ->    {"___INF___" : 1}
    -Infinity   ->    {"___INF___" : -1}
    NaN         ->    {"___NAN___" : 0}
*/
let JSONX = (function () {

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

  function encodeSpecialFloats(obj) {
    if (obj instanceof Array) return obj.map(encodeSpecialFloats);
    if (obj === null) return null;
    if (obj === undefined) return undefined;

    if (typeof obj == "number") {
      if (obj == Infinity) return {"___INF___" : 1};
      if (obj == -Infinity) return {"___INF___" : -1};
      if (isNaN(obj)) return {"___NAN___" : 0};
    }
    if (typeof obj != "object") return obj;

    let value = {};
    for (let m in obj) {
      value[m] = encodeSpecialFloats(obj[m]);
    }
    return value;
  }

  return {
    parse: str => decodeSpecialFloats(JSON.parse(str)),
    stringify: obj => JSON.stringify(encodeSpecialFloats(obj)),
  }

})();
