operators-mapping
flatten
	"Answer a new parser that flattens the underlying collection."
	
	^ PPFlattenParser on: self