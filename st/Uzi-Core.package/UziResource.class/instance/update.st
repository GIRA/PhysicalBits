initialize-release
update
	changed := true.
	sema valuesDo: [:each | each signal]