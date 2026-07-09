package org.example.artyom.mechanism.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.example.artyom.mechanism.Mechanism;

import org.example.artyom.mechanism.items.BarrierItem;
import org.example.artyom.mechanism.items.CableItem;
import org.example.artyom.mechanism.items.GeneratorItem;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MechanismCommands implements CommandExecutor {
    private final Mechanism plugin;
    private final Map<String, CommandHandler> commands = new HashMap<>();

    // Функциональный интерфейс для обработки команд
    private interface CommandHandler {
        boolean handle(Player player, String[] args);
    }

    public MechanismCommands(Mechanism plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    private void registerCommands() {
        // Регистрация всех команд с их обработчиками
        commands.put("getgen", (player, args) -> {
            int amount = parseAmount(args, 0, 1); // Парсим количество из аргументов
            ItemStack item = new GeneratorItem(plugin).createItem(amount);
            return giveItemToPlayer(player, item, "Генератор", amount);
        });

//        commands.put("givecell", (player, args) -> {
//            //int energy = parseAmount(args, 0, 150); // Парсим энергию из аргументов
//            EnergyBlock energyCell = new EnergyBlock(plugin);
//            ItemStack item = energyCell.create(); //Передавать энергию!!!!
//            player.getInventory().setItemInMainHand(item);
//            player.sendMessage("§aВы получили энергетическую ячейку с " + 500 + " энергии");
//            return true;
//        });
//
        commands.put("getbarrier", (player, args) -> {
            int amount = parseAmount(args, 0, 1);
            ItemStack item = new BarrierItem(plugin).createItem(amount);
            return giveItemToPlayer(player, item, "Барьер", amount);
        });
//
        commands.put("getcable", (player, args) -> {
            int amount = parseAmount(args, 0, 64);
            ItemStack item = new CableItem(plugin).createItem(amount);
            return giveItemToPlayer(player, item, "Кабель", amount);
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Проверяем, является ли отправитель игроком
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }

        // Проверяем права на использование команд (опционально)
        if (!player.hasPermission("magicmechanism.admin")) {
            player.sendMessage("§cУ вас нет прав на использование этих команд!");
            return true;
        }

        // Получаем обработчик для введенной команды
        CommandHandler handler = commands.get(command.getName().toLowerCase());

        if (handler != null) {
            return handler.handle(player, args);
        }

        // Если команда не найдена, показываем помощь
        sendHelpMessage(player);
        return true;
    }

    /**
     * Утилитарный метод для выдачи предметов игроку
     */
    private boolean giveItemToPlayer(Player player, ItemStack item, String itemName, int amount) {
        if (item == null) {
            player.sendMessage("§cПроизошла ошибка при создании предмета!");
            return false;
        }

        player.getInventory().addItem(item);

        // Отправляем сообщение в зависимости от количества
        if (amount > 1) {
            player.sendMessage("§aВыдан " + itemName + " в количестве: §e" + amount);
        } else {
            player.sendMessage("§aВыдан " + itemName);
        }

        return true;
    }

    /**
     * Парсит количество из аргументов команды
     * @param args аргументы команды
     * @param index индекс аргумента с количеством
     * @param defaultValue значение по умолчанию
     * @return количество или значение по умолчанию
     */
    private int parseAmount(String[] args, int index, int defaultValue) {
        if (args.length > index) {
            try {
                int amount = Integer.parseInt(args[index]);
                return Math.max(1, Math.min(amount, 64)); // Ограничиваем от 1 до 64
            } catch (NumberFormatException e) {
                // Игнорируем, используем значение по умолчанию
            }
        }
        return defaultValue;
    }

    /**
     * Отправляет сообщение с помощью по командам
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== Magic Mechanism Commands ===");
        player.sendMessage("§e/getgen [количество] §7- Получить генератор");
        player.sendMessage("§e/givecell [энергия] §7- Получить энергетическую ячейку");
        player.sendMessage("§e/getbarrier [количество] §7- Получить барьер");
        player.sendMessage("§e/getcable [количество] §7- Получить кабель");
    }

}
