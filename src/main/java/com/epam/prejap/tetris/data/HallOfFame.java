package com.epam.prejap.tetris.data;

import com.epam.prejap.tetris.game.Printer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Contains members with highest scores in Tetris game.
 *
 * @implNote: It contains List with {@link HallOfFameMember} read from file.
 */
public final class HallOfFame {

    private static final Path PATH = Paths.get("src/main/resources/files/HallOfFame.json");

    private final DataReader reader;
    private final DataWriter writer;
    private final Printer printer;

    private volatile List<HallOfFameMember> members;

    public HallOfFame(final Printer printer) {
        this.reader = new DataReader(PATH);
        this.writer = new DataWriter(PATH);
        this.printer = printer;
    }

    HallOfFame(final DataReader reader, final DataWriter writer, final Printer printer, final List<HallOfFameMember> members) {
        this.reader = reader;
        this.writer = writer;
        this.printer = printer;
        this.members = members;
    }

    private List<HallOfFameMember> obtainMembers() {
        var readList = Arrays.asList(reader.readFromFile());
        return new ArrayList<>(readList);
    }

    /**
     * Try to enter high score {@link HallOfFame} if number of points is sufficient
     *
     * @param points int representing points won in game
     * @return boolean if qualified to enter high scores
     */
    public boolean tryToEnter(final int points) {
        synchronized (this) {
            if (members == null) {
                members = obtainMembers();
            }
        }

        if (members.isEmpty()) {
            return false;
        }
        Optional<HallOfFameMember> anyDefeated = members.stream()
                .filter(e -> e.points() < points)
                .findAny();

        if (anyDefeated.isPresent()) {
            printer.newHighScore(points);
            String name = System.getProperty("user.name");
            HallOfFameMember newMember = createNewMember(points, name);
            members = enterHallOfFame(newMember);
            return true;
        }
        return false;
    }


    /**
     * Add new member to {@link HallOfFame} and save to file.
     *
     * <p>List of all members is sorted descending by points, saved to file and first 25 elements printed.
     *
     * @param newMember entitled to enter hall of fame
     * @return updated list of members
     */
    List<HallOfFameMember> enterHallOfFame(final HallOfFameMember newMember) {
        members.add(newMember);
        Collections.sort(members);
        writer.writeToFile(members.toArray(HallOfFameMember[]::new));
        printer.hallOfFame(this);
        return members;
    }


    /**
     * Creates new {@link HallOfFameMember} from given players initials and scores,
     * with name limited to max. 3 characters.
     *
     * @param score      int representing new high score
     * @param systemName Player name, shall be read from user.name property
     * @return new member of HallOfFame with given initials and score.
     */
    public HallOfFameMember createNewMember(final int score, String systemName) {
        if (systemName.length() <= 3) {
            return new HallOfFameMember(systemName, score);

        }
        return new HallOfFameMember(systemName.substring(0, 3), score);
    }


    /**
     * Creates String representation of 25 best score members of HallOfFame.
     *
     * @return String representation of 25 highest scores members.
     * @implNote: all members are limited to 25 and printed in format "[number]. Name: [name], Score: [points]".
     */
    @Override
    public String toString() {
        HallOfFameMember[] limitedMembers = limitMembersTo25elements();

        StringBuilder builder = new StringBuilder();
        builder.append("HALL OF FAME\n");
        for (int i = 0; i < limitedMembers.length; i++) {
            int order = i + 1;
            builder.append(order + ". " + limitedMembers[i] + "\n");
        }
        return builder.toString();
    }

    private HallOfFameMember[] limitMembersTo25elements() {
        return members.stream()
                .limit(25)
                .toArray(HallOfFameMember[]::new);
    }

}
