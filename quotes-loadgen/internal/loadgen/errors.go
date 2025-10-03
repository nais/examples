package loadgen

import "errors"

var (
	ErrNoURLsProvided  = errors.New("no URLs provided")
	ErrInvalidProtocol = errors.New("invalid protocol, must be http or https")
	ErrInvalidRPS      = errors.New("requests per second must be greater than 0")
)
