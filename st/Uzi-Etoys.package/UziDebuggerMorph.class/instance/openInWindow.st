building
openInWindow 
	| height |
	window := (SystemWindow labelled: ('Uzi Debugger @{1}' format: { debugger scriptName })) model: self.
	height := 30.
	window 
		addMorph: self
		fullFrame: (LayoutFrame fractions: (0@0 corner: 1.0@0.0) offsets: (0@0 corner: 0@height)).
	window 
		addMorph: (AlignmentMorph newRow
			cellPositioning: #topCenter;
			cellInset: 5;
			addMorphBack: (Text string: 'Stack:' attributes: { TextEmphasis normal }) asMorph;
			addMorphBack: ((PluggableTextMorph 
					on: self
					text: #stackString  
					accept: nil)
				borderWidth: 0;
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