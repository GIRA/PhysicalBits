messages
keywordExpression
	^ super keywordExpression map: [:receiverNode :messageNodes | 
		self build: receiverNode 
			messages: { messageNodes }]