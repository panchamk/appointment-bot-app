import { useRef, useState } from "react";
import 'regenerator-runtime';
import SpeechRecognition, { useSpeechRecognition } from "react-speech-recognition";
// import { IconMicrophone } from '@/components/ui/icons'
import "@/components/css/chatVoiceInput.css";
import { IconMicrophone } from "./ui/icons";

export function ChatVoiceInput({setTranscript}: {
    setTranscript: () => void
}) {

    const { transcript, resetTranscript } = useSpeechRecognition();
    const [isListening, setIsListening] = useState(false);
    const microphoneRef: React.RefObject<HTMLDivElement> = useRef<HTMLDivElement>(null);
    if (!SpeechRecognition.browserSupportsSpeechRecognition()) {
        return (
            <div className="mircophone-container">
                Browser doesnot not Support Speech Recognition.
            </div>
        );
    }
    const handleListing = () => {
        setIsListening(true);
        if(microphoneRef !== null && microphoneRef.current !== null && microphoneRef.current.classList !== null) {
            microphoneRef.current.classList.add("listening");
        }
        SpeechRecognition.startListening({
            continuous: true,
        });
    };
    const stopHandle = () => {
        setIsListening(false);
        if(microphoneRef !== null && microphoneRef.current !== null && microphoneRef.current.classList !== null) {
            microphoneRef.current.classList.remove("listening");
        }
        SpeechRecognition.stopListening();
        setTranscript()
    };
    const handleReset = () => {
        stopHandle();
        resetTranscript();
    };
    return (
        <div className="microphone-wrapper">
            <div className="mircophone-container">
                <div
                    className="microphone-icon-container"
                    ref={microphoneRef}
                    onClick={handleListing}
                >

                </div>
                <div className="microphone-status">
                    {isListening ? "Listening........." : "Click to start Listening"}
                </div>
                {isListening && (
                    <button className="microphone-stop btn" onClick={stopHandle}>
                        Stop
                    </button>
                )}
            </div>

            <IconMicrophone />
            {transcript && (
                <div className="microphone-result-container">
                    <div className="microphone-result-text">{transcript}</div>
                    <button className="microphone-reset btn" onClick={handleReset}>
                        Reset
                    </button>
                </div>
            )}
        </div>
    );
}