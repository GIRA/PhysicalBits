private
allSelectorsFor: class
	^ class = self class root 
		ifTrue: [class allSelectors]
		ifFalse: [class allSelectorsBelow: self class root]