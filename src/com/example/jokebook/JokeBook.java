package com.example.jokebook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.UUID;

public class JokeBook {

    private static final JokeBook sInstance = new JokeBook();
    private List<Joke> mJokes;

    public static JokeBook getInstance() {
        return sInstance;
    }

    private JokeBook() {
        // An hard-coded array of Joke objects. This is a short-term solution, and should optimally be moved to a sqlite database
        mJokes = Arrays.asList(
                // Chicken
                new Joke(UUID.randomUUID(), "Why did the chicken cross the road?", "It didn't, it was in the oven.", JokeType.CHICKEN),
                new Joke(UUID.randomUUID(), "Why did the chicken cross the Mobius strip?", "To stay on the same side", JokeType.CHICKEN),
                new Joke(UUID.randomUUID(), "Why did the rubber chicken cross the road?", "She wanted to stretch her legs", JokeType.CHICKEN),
                // Anti
                new Joke(UUID.randomUUID(), "How much does a polar bear weigh?", "Roughly 1,150 pounds if it's fully grown", JokeType.ANTI),
                new Joke(UUID.randomUUID(), "What does a duck and a tablespoon have in common?", "Both are not a lamp.", JokeType.ANTI),
                new Joke(UUID.randomUUID(), "What's orange and tastes like an orange?", "An orange.", JokeType.ANTI),
                // Lawyer
                new Joke(UUID.randomUUID(), "How does an attorney sleep?", "First he lies on one side, then he lies on the other.", JokeType.LAWYER),
                new Joke(UUID.randomUUID(), "What's the difference between a lawyer and a liar?", "The pronunciation.", JokeType.LAWYER),
                new Joke(UUID.randomUUID(), "How can you tell when a lawyer is lying?", "Their lips are moving.", JokeType.LAWYER)
        );

    }

    public static JokeType determineRequestedJokeType(List<String> jokeTypeSelection) {
        if (jokeTypeSelection == null) {
            return JokeType.DEFAULT;
        }
        //make joke type selection strings lowercase
        ListIterator<String> iterator = jokeTypeSelection.listIterator();
        while (iterator.hasNext()) {
            iterator.set(iterator.next().toLowerCase());
        }
        for (JokeType jokeType : JokeType.values()) {
            if (jokeTypeSelection.contains(jokeType.getJokeTypeStringMatch())) {
                return jokeType;
            }
        }
        return JokeType.DEFAULT;
    }

    public Joke getRandomJokeByType(List<String> jokeTypeSelection) {
        return getRandomJokeByType(determineRequestedJokeType(jokeTypeSelection));
    }

    public Joke getRandomJokeByType(JokeType jokeType) {
        List<Joke> filteredJokes = new ArrayList<Joke>();
        if (jokeType == null || jokeType == JokeType.DEFAULT) {
            filteredJokes = mJokes;
        } else {
            //filter the array
            for (Joke joke : mJokes) {
                if (joke.mJokeType == jokeType) {
                    filteredJokes.add(joke);
                }
            }
        }
        return filteredJokes.get(new Random().nextInt(filteredJokes.size()));
    }


}
