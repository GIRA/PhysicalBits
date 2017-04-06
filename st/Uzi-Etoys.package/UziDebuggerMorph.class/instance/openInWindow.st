building
openInWindow 
	| height window |
	window := (SystemWindow labelled: 'Uzi Debugger') model: self.
	height := 30.
	window 
		addMorph: self
		fullFrame: (LayoutFrame fractions: (0@0 corner: 1.0@0.0) offsets: (0@0 corner: 0@height)).
	window 
		addMorph: (AlignmentMorph newRow
			cellPositioning: #topCenter;
			cellInset: 5;
			addMorphBack: 'Stack:' asMorph;
			addMorphBack: ((UpdatingStringMorph on: self selector: #stack)
				hResizing: #spaceFill;
				vResizing: #spaceFill;
				yourself);
			yourself)
		fullFrame: (LayoutFrame fractions: (0@0 corner: 1.0@0.2) offsets: (0@height corner: 0@0)).
	window
		addMorph: (PluggableTextMorph 
			on: self
			text: #contents 
			accept: nil
			readSelection: #contentsSelection
			menu: nil)
		fullFrame: (LayoutFrame fractions: (0@0.2 corner: 1.0@1) offsets: (0@0 corner: 0@0)).
	window extent: 600 @ 400.
	window openInHand