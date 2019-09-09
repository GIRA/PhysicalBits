let XML = {
  getId: function (block) {
    return block.getAttribute("id");
  },
  getChildNode: function (block, name) {
    return XML.getLastChild(block, function (child) {
      try {
  			return child.getAttribute("name") === name;
      } catch (e) { return false; }
		});
  },
  getLastChild: function (block, predicate) {
		for (var i = block.childNodes.length - 1; i >= 0; i--) {
			var child = block.childNodes[i];
			if (predicate === undefined || predicate(child)) {
				return child;
			}
		}
		return undefined;
  }
};
