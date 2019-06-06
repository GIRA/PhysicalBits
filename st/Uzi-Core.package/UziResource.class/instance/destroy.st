initialize-release
destroy
	self protocol removeActionsWithReceiver: self.
	self out removeActionsWithReceiver: self