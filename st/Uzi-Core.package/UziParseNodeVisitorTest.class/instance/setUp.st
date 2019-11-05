private
setUp
	nodes := UziParseNode allSubclasses select: [:class | class selectors includes: #accept:].