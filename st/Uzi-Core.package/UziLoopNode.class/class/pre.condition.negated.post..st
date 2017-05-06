instance creation
pre: preAction condition: expression negated: aBoolean post: postAction
	^ self basicNew
		initializeWithPre: preAction 
		condition: expression 
		negated: aBoolean 
		post: postAction