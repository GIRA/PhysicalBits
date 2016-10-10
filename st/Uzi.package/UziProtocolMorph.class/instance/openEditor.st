button actions
openEditor
	| editor window |
	editor := UziEditor on: self.
	window := (SystemWindow labelled: 'Uzi Editor') model: editor.
	window 
		addMorph: (PluggableTextMorph 
			on: editor
			text: #contents 
			accept: #acceptContents:
			readSelection: nil 
			menu: #codePaneMenu:shifted:)
		frame: (0@0 corner: 1@1).
	window openInHand