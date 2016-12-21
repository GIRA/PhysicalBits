initialization
initialize
	"Platform independent newline sequence. LF: Unix, CR+LF: Windows, and CR: Apple."

	NewLineParser := (Character lf asParser) / (Character cr asParser , Character lf asParser optional)