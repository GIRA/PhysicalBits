building
buildInspectorButton
	| form pressedForm |
	form := ScriptingSystem formAtKey: #'Halo-Debug'.
	pressedForm := ScriptingSystem formAtKey: #'Halo-DebugPressed'.
	^ (ThreePhaseButtonMorph new
		image: form;
		offImage: form;
		pressedImage: (pressedForm ifNil: [form]))
		extent: form extent;
		target: protocol;
		actionSelector: #explore