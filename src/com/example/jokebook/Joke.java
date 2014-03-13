package com.example.jokebook;

/**
 * A Joke object to represent a joke with a question and answer
 */
public class Joke {
    public String mQuestion;
    public String mAnswer;
    public JokeType mJokeType;

    public Joke(String question, String answer, JokeType jokeType) {
        mQuestion = question;
        mAnswer = answer;
        mJokeType = jokeType;
    }

    @Override
    public String toString() {
        return "Joke{" +
                ", mQuestion=" + mQuestion +
                ", mAnswer=" + mAnswer +
                ", mJokeType=" + mJokeType +
                '}';
    }
}
