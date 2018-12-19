building
buildViewerButton
	| form pressedForm |
	form := ScriptingSystem formAtKey: #'Halo-View'.
	pressedForm := ScriptingSystem formAtKey: #'Halo-ViewPressed'.
	^ (ThreePhaseButtonMorph new
		image: form;
		offImage: form;
		pressedImage: (pressedForm ifNil: [form]))
		extent: form extent;
		target: self;
		actionSelector: #beViewed