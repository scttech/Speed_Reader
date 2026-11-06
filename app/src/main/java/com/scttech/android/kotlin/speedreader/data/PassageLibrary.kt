package com.scttech.android.kotlin.speedreader.data

import com.scttech.android.kotlin.speedreader.model.Passage
import com.scttech.android.kotlin.speedreader.model.Question

/**
 * Original passages written for this app (not taken from any book), all of similar
 * length and difficulty so baseline re-tests are comparable.
 */
object PassageLibrary {

    val baseline: List<Passage> = listOf(
        Passage(
            id = "honeybee",
            title = "Inside the Hive",
            text = """
A honeybee colony is one of the most organized societies in nature. In summer, a single hive may hold between 20,000 and 60,000 bees, yet almost all of them are sisters. At the centre of the colony lives the queen, the only bee whose job is to lay eggs. In the busiest weeks of the season she can lay as many as 2,000 eggs in a single day, and she may live for several years.

The workers, all of them female, do everything else. A young worker begins her adult life inside the hive. For her first days she cleans the cells in the honeycomb, and soon after she feeds the larvae, which are the growing young. Later she builds new wax comb, guards the entrance, and fans her wings to keep the temperature steady. Only in the last weeks of her life does she become a forager and fly out to collect nectar, pollen, and water. In summer, a worker lives only about six weeks, so the colony must constantly raise new bees to replace those that are lost.

Foragers have a remarkable way of sharing information. When a bee finds a rich patch of flowers, she returns to the hive and performs a waggle dance on the surface of the comb. The angle of her dance, measured against straight up, shows the direction of the flowers relative to the sun. The length of the waggling part tells the others how far they must fly. Other workers follow the dancer closely, then leave to find the same flowers.

The nectar the foragers carry home is mostly water and sugar. Inside the hive, house bees pass the nectar from mouth to mouth and spread it in thin layers in the cells. They fan it with their wings until much of the water has evaporated. When the mixture is thick enough, the bees seal the cell with a cap of wax. This is honey, and it is the colony's food store for winter, when no flowers are blooming.

In cold weather the bees do not hibernate. Instead they gather in a tight ball around the queen and shiver their flight muscles to make heat. Those on the outside of the cluster slowly trade places with those in the middle, so that no bee stays in the cold for too long. By spring, a healthy colony will have eaten a large part of its honey, but the queen will already have begun laying again.
""".trim(),
            questions = listOf(
                Question(
                    "About how many eggs can the queen lay in a single day at the busiest time of year?",
                    listOf("About 200", "About 2,000", "About 20,000", "About 60,000"),
                    1,
                ),
                Question(
                    "Which job does a worker bee do last in her life?",
                    listOf("Feeding the larvae in the cells of the comb", "Guarding the entrance against robber bees", "Cleaning out the cells of the honeycomb", "Foraging for nectar, pollen, and water"),
                    3,
                ),
                Question(
                    "What does the angle of the waggle dance tell the other bees?",
                    listOf("How far away the patch of flowers is from the hive", "The direction of the flowers relative to the sun", "How sweet the nectar in the flowers will taste", "How many other bees should leave the hive"),
                    1,
                ),
                Question(
                    "How do house bees turn nectar into honey?",
                    listOf("They mix it with pollen and the royal jelly", "They freeze it overnight in the coldest cells", "They fan it until much of the water evaporates", "They add a layer of wax to it, drop by drop"),
                    2,
                ),
                Question(
                    "How does the colony keep warm in cold weather?",
                    listOf("The bees hibernate separately in the empty cells of the comb", "The bees cluster around the queen and shiver their flight muscles", "The bees leave the hive and fly south to warmer places", "The queen produces heat all by herself by beating her wings in the middle"),
                    1,
                ),
            ),
        ),

        Passage(
            id = "lighthouse",
            title = "How Lighthouses Work",
            text = """
For centuries, sailors relied on simple fires built on headlands to warn them of dangerous coasts. The first great lighthouse, built on the island of Pharos near Alexandria in Egypt, is thought to have stood well over a hundred metres tall. Yet for most of history, the light itself was weak. Wood and coal fires, and later oil lamps, produced a glow that could easily be lost in rain or fog.

The great breakthrough came in the 1820s, when the French physicist Augustin-Jean Fresnel designed a new kind of lens. A traditional lens for a lighthouse had to be thick and heavy to bend light, and the bigger it was, the more glass it needed. Fresnel solved this by building the lens from many separate rings of glass prisms, each set at a carefully chosen angle. The design was far lighter and thinner, but it gathered light from the lamp and sent it out in a single strong beam. A Fresnel lens could make a lamp visible from more than thirty kilometres away on a clear night. One of the first to use the new lens was the Cordouan lighthouse, at the mouth of a river in western France, in 1823.

A beam alone is not enough, because sailors need to know which lighthouse they are looking at. For this reason, each one has its own signal, called its characteristic. Some lights flash once every ten seconds. Others flash twice, then pause, and others show a steady glow in a particular colour. A captain can find the pattern on a chart and know at once where the ship is. Before the invention of radio navigation and satellites, this simple code was one of the most important tools at sea.

Life for the keepers was demanding. They climbed hundreds of steps to trim wicks, polish brass, and wind the clockwork that turned the lens. In remote stations, supplies might come only a few times a year, and bad weather could leave a keeper's family alone for weeks. Electric lamps arrived in the late nineteenth century and made much of this work unnecessary. By the end of the twentieth century, most lighthouses around the world had been automated, and the keepers had gone.

Today many lighthouses are protected as historic buildings. Ships now depend largely on satellite navigation, but the old towers are still lit, since a good light is a useful backup if electronic systems fail.
""".trim(),
            questions = listOf(
                Question(
                    "Who designed the new kind of lens in the 1820s?",
                    listOf("An Egyptian engineer", "A Greek astronomer", "A French physicist", "An English sailor"),
                    2,
                ),
                Question(
                    "How did the new design make the lens lighter?",
                    listOf("By using many separate rings of glass prisms", "By making the whole lens out of clear plastic", "By using a much smaller lamp inside the tower", "By removing the glass entirely from the middle of the lens"),
                    0,
                ),
                Question(
                    "What is a lighthouse's \"characteristic\"?",
                    listOf("The total height of its tower above the sea", "The colour of the paint on its tower walls", "The timetable of work kept by its keepers", "Its own pattern of light signals"),
                    3,
                ),
                Question(
                    "What made much of the keepers' work unnecessary?",
                    listOf("Radio navigation", "Electric lamps", "Larger ships", "The Fresnel lens"),
                    1,
                ),
                Question(
                    "Why are many lighthouses still lit today?",
                    listOf("Ships out at sea still cannot use satellite navigation at all", "The law requires every tower to stay lit by night", "A good light is a useful backup if electronic systems fail", "They attract many tourists who pay to climb them"),
                    2,
                ),
            ),
        ),

        Passage(
            id = "sleep",
            title = "The Science of Sleep",
            text = """
Sleep may look like a time when the body simply switches off, but the brain stays busy through the whole night. A typical night is made up of several sleep cycles, each lasting about ninety minutes. In each cycle the sleeper moves from light sleep into deep sleep and then into a stage known as REM, short for rapid eye movement. Most vivid dreams happen during REM sleep, when the eyes dart back and forth beneath closed lids, even though the muscles of the body are almost completely relaxed.

Deep sleep and REM sleep seem to do different jobs. During deep sleep, the body repairs tissue and the brain clears away waste that builds up during the day. During REM sleep, the brain appears to sort through the day's experiences, strengthening important memories and letting go of those that matter less. Students who sleep well after studying often remember more than those who stay up all night, which suggests that rest is part of learning rather than a break from it. Because deep sleep is concentrated in the first half of the night and REM in the second half, cutting a night short tends to rob a person of REM sleep in particular.

Two main forces decide when we feel sleepy. The first is a chemical called adenosine, which slowly builds up in the brain while we are awake. The longer we stay up, the more adenosine we have, and the stronger our urge to sleep becomes. Caffeine works by blocking the receptors that detect adenosine, so the brain does not notice how tired it really is. The effect wears off after several hours, and the sleepiness that was hidden returns.

The second force is the body clock, or circadian rhythm, which runs on a cycle of roughly twenty-four hours. Light is its most powerful signal. When daylight reaches the eyes in the morning, the clock is reset, and as evening comes, the brain begins to release melatonin, a hormone that prepares the body for sleep. Bright screens late at night can delay this release, which is one reason that many people find it hard to fall asleep after using a phone in bed.

Most adults need between seven and nine hours of sleep each night, although the exact amount varies from person to person. Going to bed and waking at regular times, keeping the bedroom dark and cool, and avoiding caffeine in the afternoon are some of the simplest ways to improve sleep without any medical help.
""".trim(),
            questions = listOf(
                Question(
                    "About how long does a typical sleep cycle last?",
                    listOf("Thirty minutes", "Three hours", "Ninety minutes", "Eight hours"),
                    2,
                ),
                Question(
                    "What happens during REM sleep, according to the passage?",
                    listOf("The eyes dart back and forth while the body's muscles are relaxed", "The body repairs tissue and the brain clears away waste products", "The brain stops all activity until the morning light", "The body temperature rises sharply and the heart races at top speed"),
                    0,
                ),
                Question(
                    "How does caffeine make a person feel less tired?",
                    listOf("It removes all of the adenosine from the brain altogether", "It releases a hormone called melatonin", "It blocks the receptors that detect adenosine", "It resets the body clock by several hours"),
                    2,
                ),
                Question(
                    "Why can bright screens late at night make it harder to fall asleep?",
                    listOf("They increase the amount of adenosine in the brain", "They can delay the release of melatonin", "They shorten the stage of REM sleep", "They raise the temperature of the bedroom"),
                    1,
                ),
                Question(
                    "Which of these is suggested as a simple way to improve sleep?",
                    listOf("Sleeping late every single weekend to catch up", "Keeping the bedroom bright and warm", "Drinking coffee in the evening", "Going to bed and waking at regular times"),
                    3,
                ),
            ),
        ),

        Passage(
            id = "bicycle",
            title = "The Rise of the Bicycle",
            text = """
The bicycle as we know it took almost a century to develop. In 1817, a German baron named Karl von Drais built a wooden machine with two wheels in line and a steerable front wheel. It had no pedals. The rider sat on a small saddle and pushed along the ground with both feet. The machine, called the draisine, was fast on smooth paths and became a short-lived fashion in Europe, but it was awkward on rough roads and was soon forgotten.

Pedals arrived in the 1860s, when French inventors fixed them directly to the front wheel. Because one turn of the pedals meant one turn of the wheel, a bigger wheel meant a higher speed. This led to the penny-farthing of the 1870s, whose front wheel could be well over a metre and a half across. These bicycles were fast, but they were also dangerous. The rider sat high above the ground, and a stone in the road could throw him headfirst over the handlebars. Most riders were young, athletic men.

The change that made cycling safe came in the 1880s. In 1885, the English maker John Kemp Starley produced the Rover, a bicycle with two wheels of nearly equal size and a chain that carried power to the rear wheel. Because the chain could turn the back wheel several times for every turn of the pedals, a small wheel could still be fast. The rider sat low, close to the ground, and could put both feet down at a stop. This design, called the safety bicycle, is the ancestor of nearly every bicycle today.

Comfort improved in 1888, when a Scottish veterinarian named John Boyd Dunlop fitted his son's tricycle with tyres filled with air. The new tyres softened the shaking of the road and were also faster, and within a few years they were standard on racing machines and ordinary bicycles alike.

The bicycle had large effects on society. It gave working people a cheap way to travel beyond their own neighbourhoods, and it gave many women their first chance to travel alone. Cyclists also demanded better roads, and their campaigns in the 1890s helped to pave the way for the smooth highways later used by cars.
""".trim(),
            questions = listOf(
                Question(
                    "What did Karl von Drais's machine lack?",
                    listOf("A steerable wheel", "A saddle", "Pedals", "Two wheels"),
                    2,
                ),
                Question(
                    "Why were penny-farthings dangerous?",
                    listOf("Their wheels often broke under the weight of the rider", "The rider sat high and could be thrown over the handlebars", "They had no brakes or chain to drive them", "They were far too heavy for most riders to steer on rough roads"),
                    1,
                ),
                Question(
                    "What did the chain on the safety bicycle allow?",
                    listOf("A small wheel could still be fast", "The rider to avoid pedalling", "The front wheel to be made larger", "The bicycle to carry heavier loads"),
                    0,
                ),
                Question(
                    "What did John Boyd Dunlop fit to his son's tricycle?",
                    listOf("A chain drive", "Tyres filled with air", "A steerable front wheel", "A wooden frame"),
                    1,
                ),
                Question(
                    "How did cyclists help prepare the way for cars?",
                    listOf("They invented the engine", "They built the first factories", "They designed the first road signs", "They campaigned for smoother roads"),
                    3,
                ),
            ),
        ),
    )
}
