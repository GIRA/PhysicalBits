initialize-release
destroy
	self controller removeActionsWithReceiver: self.
	Uzi logger removeActionsWithReceiver: self