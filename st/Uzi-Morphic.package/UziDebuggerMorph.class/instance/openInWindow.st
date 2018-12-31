building
openInWindow 
	| height |
	window := (SystemWindow labelled: 'Uzi Debugger') model: self.
	height := 30.
	window 
		addMorph: self
		fullFrame: (LayoutFrame fractions: (0@0 corner: 1.0@0.0) offsets: (0@0 corner: 0@height)).
	window 
		addMorph: (PluggableListMorph
			on: self
			list: #stackFrames
			selected: #stackFrameSelection 
			changeSelected: #stackFrameSelection:)
		fullFrame: (LayoutFrame fractions: (0@0 corner: 1.0@0.2) offsets: (0@height corner: 0@0)).
	window
		addMorph: (PluggableTextMorph 
			on: self
			text: #sourceCode
			accept: nil
			readSelection: #sourceCodeSelection
			menu: nil)
		fullFrame: (LayoutFrame fractions: (0@0.2 corner: 0.55@0.8) offsets: (0@0 corner: 0@0)).
	window
		addMorph: (PluggableTextMorph 
			on: self
			text: #textualForm
			accept: nil
			readSelection: #textualFormSelection 
			menu: nil)
		fullFrame: (LayoutFrame fractions: (0.55@0.2 corner: 1.0@0.8) offsets: (0@0 corner: 0@0)).
	window 
		addMorph: (AlignmentMorph newRow
			color: Color white;
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
		fullFrame: (LayoutFrame fractions: (0@0.8 corner: 0.55@1.0) offsets: (0@0 corner: 0@0)).
	window
		addMorph: (PluggableTextMorph 
			on: self
			text: #locals
			accept: nil
			readSelection: nil
			menu: nil)
		fullFrame: (LayoutFrame fractions: (0.55@0.8 corner: 1.0@1.0) offsets: (0@0 corner: 0@0)).
	"HACK(Richo): To make the selection more visible I change the selection color of
	all submorphs to an ugly yellow."
	window submorphsDo: [:m || selectionColor |
		selectionColor := Color yellow alpha: 0.5.
		(m isKindOf: PluggableTextMorph)
			ifTrue: [	m textMorph 
				unfocusedSelectionColor: selectionColor;
				selectionColor: selectionColor]].
	window extent: 600 @ 600.
	window openInHand