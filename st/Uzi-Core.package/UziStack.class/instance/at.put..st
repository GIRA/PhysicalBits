private
at: index put: obj
	"index is zero-based"
	elements at: index + 1 put: obj