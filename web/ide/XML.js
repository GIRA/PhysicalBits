let XML = {
  getId: function (block) {
    return block.getAttribute("id");
  },
  getChildNode: function (block, name) {
    return XML.getLastChild(block, function (child) {
			return child.getAttribute("name") === name;
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
