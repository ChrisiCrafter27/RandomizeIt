package de.chrisicrafter.randomizeit.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.chrisicrafter.skillscreenapi.SkillScreenApi;
import de.chrisicrafter.skillscreenapi.common.data.PlayerSkills;
import de.chrisicrafter.skillscreenapi.common.data.PlayerSkillsProvider;
import de.chrisicrafter.skillscreenapi.common.skills.SkillHolder;
import de.chrisicrafter.skillscreenapi.common.skills.SkillNode;
import de.chrisicrafter.skillscreenapi.common.skills.SkillProgress;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public class SkillCommand extends PermissionLevel {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SKILLS = (context, builder) -> {
        Collection<SkillHolder> collection = SkillScreenApi.getSkillManager().getAllSkills();
        return SharedSuggestionProvider.suggestResource(collection.stream().map(SkillHolder::id), builder);
    };
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ROOTS = (context, builder) -> {
        Collection<SkillHolder> collection = SkillScreenApi.getSkillManager().getAllSkills();
        return SharedSuggestionProvider.suggestResource(collection.stream().filter(skill -> skill.value().getParents().isEmpty()).map(SkillHolder::id), builder);
    };

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("skill")
                .requires(context -> context.hasPermission(PERMISSION_LEVEL_CHEAT))
                .then(Commands.literal("reset")
                        .requires(context -> context.isPlayer() && context.hasPermission(PERMISSION_LEVEL_ADMIN))
                        .executes(context -> reset(context, List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(context -> reset(context, EntityArgument.getPlayers(context, "players")))))
                .then(Commands.literal("state")
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("skill", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_SKILLS)
                                                .executes(context -> get(context, EntityArgument.getPlayer(context, "player"), ResourceLocationArgument.getId(context, "skill"))))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.literal("only")
                                                .then(Commands.argument("skill", ResourceLocationArgument.id())
                                                        .suggests(SUGGEST_SKILLS)
                                                        .then(Commands.argument("state", EnumArgument.enumArgument(State.class))
                                                                .executes(context -> set(context, EntityArgument.getPlayers(context, "players"), ResourceLocationArgument.getId(context, "skill"), context.getArgument("state", State.class))))))
                                        .then(Commands.literal("from")
                                                .then(Commands.argument("skill", ResourceLocationArgument.id())
                                                        .suggests(SUGGEST_SKILLS)
                                                        .then(Commands.argument("state", EnumArgument.enumArgument(State.class))
                                                                .executes(context -> setFrom(context, EntityArgument.getPlayers(context, "players"), ResourceLocationArgument.getId(context, "skill"), context.getArgument("state", State.class)))))
                                                .then(Commands.literal("root")
                                                        .then(Commands.argument("skill", ResourceLocationArgument.id())
                                                                .suggests(SUGGEST_ROOTS)
                                                                .then(Commands.argument("state", EnumArgument.enumArgument(State.class))
                                                                        .executes(context -> setFrom(context, EntityArgument.getPlayers(context, "players"), ResourceLocationArgument.getId(context, "skils"), context.getArgument("state", State.class)))))))
                                        .then(Commands.literal("everything")
                                                .then(Commands.argument("state", EnumArgument.enumArgument(State.class))
                                                        .executes(context -> setAll(context, EntityArgument.getPlayers(context, "players"), context.getArgument("state", State.class))))))));
    }

    private static int reset(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        if(players.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.skillscreenapi.reset.fail"));
            return 0;
        }
        for (ServerPlayer player : players) {
            PlayerSkills playerSkills = player.getCapability(PlayerSkillsProvider.PLAYER_SKILLS).orElseThrow(NoSuchElementException::new);
            for(SkillHolder skill : SkillScreenApi.getSkillManager().getAllSkills()) {
                playerSkills.changeProgress(skill, SkillProgress.LOCKED, player);
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.reset.success", players.size() > 1 ? (players.size() + " players") : context.getSource().getDisplayName()), true);
        return 1;
    }

    private static int get(CommandContext<CommandSourceStack> context, ServerPlayer player, ResourceLocation id) {
        PlayerSkills playerSkills = player.getCapability(PlayerSkillsProvider.PLAYER_SKILLS).orElseThrow(NoSuchElementException::new);
        for(SkillHolder skill : SkillScreenApi.getSkillManager().getAllSkills()) {
            if(skill.id().equals(id)) {
                switch (playerSkills.getOrStartProgress(skill)) {
                    case EXCLUDING -> {
                        context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.get.success", skill.value().display().getTitle(), player.getDisplayName(), "excluded by others", 1), false);
                        return 1;
                    }
                    case LOCKED -> {
                        context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.get.success", skill.value().display().getTitle(), player.getDisplayName(), "locked", 2), false);
                        return 2;
                    }
                    case LOCKED_PARENTS -> {
                        context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.get.success", skill.value().display().getTitle(), player.getDisplayName(), "locked by it's parent(s)", 3), false);
                        return 3;
                    }
                    case AVAILABLE -> {
                        context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.get.success", skill.value().display().getTitle(), player.getDisplayName(), "available", 4), false);
                        return 4;
                    }
                    case UNLOCKED -> {
                        context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.get.success", skill.value().display().getTitle(), player.getDisplayName(), "unlocked", 5), false);
                        return 5;
                    }
                }
            }
        }
        throw new CommandRuntimeException(Component.translatable("commands.skillscreenapi.get.fail", id));
    }

    private static int set(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, ResourceLocation id, State state) {
        if(players.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.skillscreenapi.reset.fail"));
            return 0;
        }
        for(SkillHolder skill : SkillScreenApi.getSkillManager().getAllSkills()) {
            boolean success = false;
            for (ServerPlayer player : players) {
                PlayerSkills playerSkills = player.getCapability(PlayerSkillsProvider.PLAYER_SKILLS).orElseThrow(NoSuchElementException::new);
                if(skill.id().equals(id)) {
                    if(skill.value().immediatelyAvailable() && state == State.locked) {
                        context.getSource().sendFailure(Component.translatable("commands.skillscreenapi.set.available", skill.value().display().getTitle()));
                        return 0;
                    }
                    playerSkills.changeProgress(skill, switch (state) {
                        case locked -> SkillProgress.LOCKED;
                        case available -> SkillProgress.AVAILABLE;
                        case unlocked -> SkillProgress.UNLOCKED;
                    }, player);
                    success = true;
                }
            }
            if(success) {
                context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.set.success", skill.value().display().getTitle(), players.size() > 1 ? (players.size() + " players") : context.getSource().getDisplayName(), state.name()), true);
                return 1;
            }
        }
        context.getSource().sendFailure(Component.translatable("commands.skillscreenapi.set.fail", id));
        return 0;
    }

    private static int setFrom(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, ResourceLocation id, State state) {
        if(players.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.skillscreenapi.reset.fail"));
            return 0;
        }

        for(SkillHolder from : SkillScreenApi.getSkillManager().getAllSkills()) {
            if(from.id().equals(id)) {
                List<SkillNode> parents = new ArrayList<>(List.of((SkillNode) SkillScreenApi.getSkillManager().tree().nodes().stream().filter(node -> node.holder() == from).toArray()[0]));
                List<SkillNode> children = new ArrayList<>(parents);
                while (!parents.isEmpty()) {
                    List<SkillNode> temp = new ArrayList<>();
                    for(SkillNode parent : parents) {
                        temp.addAll(parent.children());
                    }
                    children.addAll(temp);
                    parents = temp;
                }

                for (ServerPlayer player : players) {
                    PlayerSkills playerSkills = player.getCapability(PlayerSkillsProvider.PLAYER_SKILLS).orElseThrow(NoSuchElementException::new);
                    for(SkillHolder skill : SkillScreenApi.getSkillManager().getAllSkills()) {
                        if(children.stream().map(SkillNode::holder).anyMatch(holder -> holder == skill)) {
                            playerSkills.changeProgress(skill, switch (state) {
                                case locked -> SkillProgress.LOCKED;
                                case available -> SkillProgress.AVAILABLE;
                                case unlocked -> SkillProgress.UNLOCKED;
                            }, player);
                        }
                    }
                }

                context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.set_from.success", from.value().display().getTitle(), players.size() > 1 ? (players.size() + " players") : context.getSource().getDisplayName(), state), true);
                return 1;
            }
        }
        context.getSource().sendFailure(Component.translatable("commands.skillscreenapi.set.fail", id));
        return 0;
    }

    private static int setAll(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, State state) {
        if(players.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.skillscreenapi.reset.fail"));
            return 0;
        }
        for (ServerPlayer player : players) {
            PlayerSkills playerSkills = player.getCapability(PlayerSkillsProvider.PLAYER_SKILLS).orElseThrow(NoSuchElementException::new);
            for(SkillHolder skill : SkillScreenApi.getSkillManager().getAllSkills()) {
                playerSkills.changeProgress(skill, switch (state) {
                    case locked -> SkillProgress.LOCKED;
                    case available -> SkillProgress.AVAILABLE;
                    case unlocked -> SkillProgress.UNLOCKED;
                }, player);
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.skillscreenapi.set_all.success", players.size() > 1 ? (players.size() + " players") : context.getSource().getDisplayName(), state), true);
        return 1;
    }

    public enum State {
        locked,
        available,
        unlocked
    }
}
