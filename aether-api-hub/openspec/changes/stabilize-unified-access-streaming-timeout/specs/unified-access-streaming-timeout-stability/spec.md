## ADDED Requirements

### Requirement: Streaming-capable targets MUST use streaming-safe forwarding
The system MUST forward responses for streaming-capable targets without requiring the full upstream response body to be buffered before returning data to the caller.

#### Scenario: Streaming response is passed through incrementally
- **WHEN** Unified Access invokes a streaming-capable target that returns a streaming response
- **THEN** the system returns a streaming response path rather than waiting for the full upstream body

### Requirement: Streaming timeout behavior MUST support long-lived responses
The system MUST avoid applying an ordinary short request/response timeout in a way that breaks valid long-lived streaming responses.

#### Scenario: Long-lived stream does not fail during valid activity
- **WHEN** a streaming-capable upstream keeps the stream open and emits valid data within the configured streaming budget
- **THEN** Unified Access does not classify the call as timed out merely because it is long-lived

#### Scenario: Stream setup timeout is classified
- **WHEN** a streaming-capable upstream fails to establish a response within the configured setup timeout
- **THEN** Unified Access returns a stable upstream timeout outcome

### Requirement: Streaming success MUST remain unwrapped
The system MUST preserve successful upstream streaming payload semantics and MUST NOT wrap successful stream data in TML Result.

#### Scenario: Successful stream is not Result-wrapped
- **WHEN** a streaming-capable target returns a successful stream
- **THEN** the caller receives the upstream stream data without TML Result wrapping
