initialize-release
destroy
	self protocol removeActionsWithReceiver: self.
	Uzi logger removeActionsWithReceiver: self