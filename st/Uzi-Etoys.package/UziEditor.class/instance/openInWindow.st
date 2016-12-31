initializing
openInWindow 
	| window |
	window := (SystemWindow labelled: 'Uzi Editor') model: self.
	window 
		addMorph: (PluggableTextMorph 
			on: self
			text: #contents 
			accept: #acceptContents:
			readSelection: #contentsSelection 
			menu: #codePaneMenu:shifted:)
		frame: (0@0 corner: 1@1).
	window openInHand