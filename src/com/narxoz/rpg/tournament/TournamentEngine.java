package com.narxoz.rpg.tournament;

import com.narxoz.rpg.arena.ArenaFighter;
import com.narxoz.rpg.arena.ArenaOpponent;
import com.narxoz.rpg.arena.TournamentResult;
import com.narxoz.rpg.chain.ArmorHandler;
import com.narxoz.rpg.chain.BlockHandler;
import com.narxoz.rpg.chain.DefenseHandler;
import com.narxoz.rpg.chain.DodgeHandler;
import com.narxoz.rpg.chain.HpHandler;
import com.narxoz.rpg.command.ActionQueue;
import com.narxoz.rpg.command.AttackCommand;
import com.narxoz.rpg.command.DefendCommand;
import com.narxoz.rpg.command.HealCommand;
import java.util.Random;

public class TournamentEngine {
    private final ArenaFighter hero;
    private final ArenaOpponent opponent;
    private Random random = new Random(1L);

    public TournamentEngine(ArenaFighter hero, ArenaOpponent opponent) {
        this.hero = hero;
        this.opponent = opponent;
    }

    public TournamentEngine setRandomSeed(long seed) {
        this.random = new Random(seed);
        return this;
    }

    public TournamentResult runTournament() {
        TournamentResult result = new TournamentResult();
        int round = 0;
        final int maxRounds = 20;

        // TODO: Build the defense chain using fluent setNext():
        //   DodgeHandler -> BlockHandler -> ArmorHandler -> HpHandler
        // Hint: use hero stats for each handler's parameters.
        //   new DodgeHandler(hero.getDodgeChance(), <seed>)
        //   new BlockHandler(hero.getBlockRating() / 100.0)   <-- note the int-to-double conversion
        //   new ArmorHandler(hero.getArmorValue())
        //   new HpHandler()
        // Chain them: dodge.setNext(block).setNext(armor).setNext(hp)
        DefenseHandler dodge = new DodgeHandler(hero.getDodgeChance(), random.nextLong());
        DefenseHandler block = new BlockHandler(hero.getBlockRating() / 100.0);
        DefenseHandler armor = new ArmorHandler(hero.getArmorValue());
        DefenseHandler hp = new HpHandler();

        dodge.setNext(block).setNext(armor).setNext(hp);

        ActionQueue actionQueue = new ActionQueue();

        while (hero.isAlive() && opponent.isAlive() && round < maxRounds) {
            round++;

            actionQueue.enqueue(new AttackCommand(opponent, hero.getAttackPower()));
            actionQueue.enqueue(new HealCommand(hero, 10)); // fixed heal amount
            actionQueue.enqueue(new DefendCommand(hero, 0.1)); // small dodge boost

            System.out.println("[Round " + round + " Hero Queue] " + actionQueue.getCommandDescriptions());

            actionQueue.executeAll();

            if (opponent.isAlive()) {
                dodge.handle(opponent.getAttackPower(), hero);
            }

            String line = "[Round " + round + "] Opponent HP: " + opponent.getHealth()
                    + " | Hero HP: " + hero.getHealth();
            System.out.println(line);
            result.addLine(line);
        }

        result.setWinner(hero.isAlive() ? hero.getName() : opponent.getName());
        result.setRounds(round);
        return result;
    }
}
