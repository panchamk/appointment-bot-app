import { useState, useCallback, useRef, useId } from 'react';
import useSWR from 'swr';
//import {initChatSession, userQuery, closeQuery} from '@/lib/serverClient'

export interface Message {
    id?: string;
    content: string;
    role?: string;
    createdAt?: Date;
}
export interface UserChatSessionOptions {
    id?: string
    api?: string;
    initialMessages?: Message[];
    initialInput?: string;
    onResponse?: (response: any) => void;
    onFinish?: () => void;
    onError?: (err: any) => void;
}


export interface UserQueryOptions {
    session: ChatSession | null;
    userQuery: UserQuery;
    appendMessage: (msg: Message) => void
    headers?: any,
    onFinish?: () => void
}

export type ChatSession = {
    "X-appt-session-id": string
    doctorId: string
}


export interface UserQuery {
    message: string,
    id: string,
}

export async function initChatSession() {
    const initSessionObj = {
        "doctorName": "Archie",
        "doctorEmail": "DrArchie@gmail.com",
        "specialization": "dentist"
    }
    const response = await fetch(
        "/v1/appointment/init", {
        method: "POST",
        body: JSON.stringify(initSessionObj),
        headers: {
            "Content-Type": "application/JSON"
        }
    }
    ).catch((err) => {
        throw err;
    });
    if (!response.ok) {
        throw new Error(
            await response.text() || "Failed to fetch the chat response."
        );
    }
    if (!response.body) {
        throw new Error("The response body is empty.");
    }
    return response.json();
}


async function userQuery({ session, userQuery, headers, appendMessage }: UserQueryOptions) {
    if (session === null) {
        throw new Error('No active session found');
    }
    const response = await fetch(
        "/v1/appointment/" + session.doctorId + "/query",
        {
            method: "POST",
            body: JSON.stringify(userQuery),
            headers: {
                "Content-Type": "application/JSON",
                "X-appt-session-id": session['X-appt-session-id'],
                ...headers
            }
        }
    ).catch((err) => {
        throw err;
    });
    if (!response.ok) {
        throw new Error(
            await response.text() || "Failed to fetch the chat response."
        );
    }
    if (!response.body) {
        throw new Error("The response body is empty.");
    }
    // const reader = response.body.getReader();
    // const createdAt = /* @__PURE__ */ new Date();
    // const decode = createChunkDecoder();
    // let streamedResponse = "";
    // const responseMessage: Message = {
    //     createdAt,
    //     content: "",
    //     role: "assistant"
    // };
    // while (true) {
    //     const { done, value } = await reader.read();
    //     if (done) {
    //         break;
    //     }
    //     streamedResponse += decode(value);

    // }
    const createdAt = /* @__PURE__ */ new Date();
    const responseMessage: Message = await response.json();
    appendMessage({ id: userQuery.id, createdAt, ...responseMessage });
    return responseMessage;
}

async function closeQuery(doctorId: string, headers: any) {
}

export function UserChatSession({
    api = "/v1/appointment",
    initialMessages,
    initialInput = "",
    onFinish,
    onError
}: UserChatSessionOptions = {}) {
    const abortControllerRef = useRef<AbortController | null>(null);
    const sessionRef = useRef<ChatSession | null>(null);
    const chatKey = [api, useId()];
    const [initialMessagesFallback] = useState<Message[]>([]);
    const { data: messages, mutate } = useSWR([chatKey, "messages"], null, {
        fallbackData: initialMessages != null ? initialMessages : initialMessagesFallback
    });
    const messagesRef = useRef<Message[]>(messages || []);
    const { data: isLoading = false, mutate: mutateLoading } = useSWR([chatKey, "loading"], null);

    const generateId = useCallback(() => {
        return Math.random().toString(36).substring(7);
    }, []);

    const [input, setInput] = useState<string>(initialInput);
    const [error, setError] = useState<any>(null);

    const reload = useCallback(async () => {
        if (sessionRef.current != null) {
            await closeQuery(
                sessionRef.current.doctorId,
                { 'X-appt-session-id': sessionRef.current['X-appt-session-id'] }
            )
        }
        sessionRef.current = await initChatSession()
        messagesRef.current = [];
        mutate([])
        return sessionRef.current
    }, [initChatSession]);
    const session = useCallback(async () => {
        if (sessionRef.current == null) {
            return await reload()
        }
        return sessionRef.current
    }, [reload])

    const append = useCallback(async (message: Message) => {
        try {

            const currentSession = await session();
            messagesRef.current = [...messagesRef.current, message]
            mutateLoading(true)
            mutate(messagesRef.current, false)
            const res = await userQuery({
                session: currentSession,
                userQuery: {
                    message: message.content,
                    id: generateId(),
                },
                appendMessage(message) {
                    messagesRef.current = [...messagesRef.current, message]
                    mutate(messagesRef.current, false);
                },
                onFinish
            });
            // debugger
            return res.content;
        } catch (err: any) {
            if (err.name === "AbortError") {
                abortControllerRef.current = null;
                return null;
            }
            if (onError && err instanceof Error) {
                onError(err);
            }
            setError(err);
        } finally {
            mutateLoading(false);
        }
    }, [userQuery]);


    const stop = useCallback(() => {
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
            abortControllerRef.current = null;
            sessionRef.current = null;
        }
    }, []);

    const handleSubmit = useCallback((e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (!input)
            return;

        append({ content: input });

        setInput("");
    }, [input, append, generateId]);

    const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        setInput(e.target.value);
    }, []);

    return {
        messages: messages || [],
        error,
        append,
        reload,
        stop,
        input,
        setInput,
        handleInputChange,
        handleSubmit,
        sessionRef,
        isLoading
    };
}
export type UseChatHelpers = {
    /** Current messages in the chat */
    messages: Message[];
    /** The error object of the API request */
    error: undefined | Error;
    /**
     * Append a user message to the chat list. This triggers the API call to fetch
     * the assistant's response.
     * @param message The message to append
     * @param options Additional options to pass to the API call
     */
    append: (message: Message) => Promise<string | null | undefined>;
    /**
     * Reload the last AI chat response for the given chat history. If the last
     * message isn't from the assistant, it will request the API to generate a
     * new response.
     */
    reload: () => void;
    /**
     * Abort the current request immediately, keep the generated tokens if any.
     */
    stop: () => void;
    /**
     * Update the `messages` state locally. This is useful when you want to
     * edit the messages on the client, and then trigger the `reload` method
     * manually to regenerate the AI response.
     */
    setMessages: (messages: Message[]) => void;
    /** The current value of the input */
    input: string;
    /** setState-powered method to update the input value */
    setInput: React.Dispatch<React.SetStateAction<string>>;
    /** An input/textarea-ready onChange handler to control the value of the input */
    handleInputChange: (e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) => void;
    /** Form submission handler to automatically reset input and append a user message */
    handleSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    sessionRef: ChatSession;
    /** Whether the API request is in progress */
    isLoading: boolean;
};