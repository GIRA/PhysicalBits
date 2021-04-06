/*
HACK(Richo): This *very* simple function will work with plain objects, arrays,
strings, and numbers. But it doesn't support any fancy features. It won't work
with functions, or actual objects, or cyclic references, or maps, or sets, etc.
*/
function deepClone(obj) {
  if (obj instanceof Array) return obj.map(deepClone);
  if (typeof obj != "object") return obj;
  if (obj === null) return null;
  if (obj === undefined) return undefined;

  let value = {};
  for (let m in obj) {
    value[m] = deepClone(obj[m]);
  }
  return value;
}
