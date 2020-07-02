let XML = {
  getId: function (block) {
    return block.getAttribute("id");
  },
  getChildNode: function (block, name, attributeName) {
    return XML.getLastChild(block, function (child) {
      try {
  			return child.getAttribute(attributeName || "name") === name;
      } catch (e) { return false; }
		});
  },
  getLastChild: function (block, predicate) {
		for (let i = block.childNodes.length - 1; i >= 0; i--) {
			let child = block.childNodes[i];
			if (predicate === undefined || predicate(child)) {
				return child;
			}
		}
		return undefined;
  }
};
