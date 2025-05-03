/**
 * Copyright 2025 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.jeddict.ai.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Gaurav Gupta
 */
public class RandomTweetSelector {

    public static List<String> urlList
            = List.of(
                    // 🌟 Code faster and smarter with Jeddict AI Assistant — your all-in-one solution for intelligent suggestions, autocompletions, and contextual insights.
                    "https://twitter.com/intent/post?text=%F0%9F%8C%9F%20Code%20faster%20and%20smarter%20with%20Jeddict%20AI%20Assistant%20%E2%80%94%20your%20all-in-one%20solution%20for%20intelligent%20suggestions%2C%20autocompletions%2C%20and%20contextual%20insights.%0A%0A%40ImJeddict%0A%0A&url=https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI",
                    // ✨ @java devs, meet your new best friend: Jeddict AI Assistant. Autocomplete, generate, and refactor smarter — all within @netbeans .
                    "https://twitter.com/intent/post?text=%E2%9C%A8%20%40java%20devs%2C%20meet%20your%20new%20best%20friend%3A%20Jeddict%20AI%20Assistant.%20Autocomplete%2C%20generate%2C%20and%20refactor%20smarter%20%E2%80%94%20all%20within%20%40netbeans%20.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🧠 Writing boilerplate code? Let Jeddict AI Assistant handle it — you focus on building logic, we handle the rest.
                    "https://twitter.com/intent/post?text=%F0%9F%A7%A0%20Writing%20boilerplate%20code%3F%20Let%20Jeddict%20AI%20Assistant%20handle%20it%20%E2%80%94%20you%20focus%20on%20building%20logic%2C%20we%20handle%20the%20rest.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🚀 Jeddict AI Assistant: Your coding companion for faster, smarter development. Autocomplete, generate, and refactor with ease.
                    "https://twitter.com/intent/post?text=%F0%9F%9A%80%20Jeddict%20AI%20Assistant%3A%20Your%20coding%20companion%20for%20faster%2C%20smarter%20development.%20Autocomplete%2C%20generate%2C%20and%20refactor%20with%20ease.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 💡 Tired of repetitive coding tasks? Jeddict AI Assistant is here to help. Autocomplete, generate, and refactor with a single click.
                    "https://twitter.com/intent/post?text=%F0%9F%92%A1%20Tired%20of%20repetitive%20coding%20tasks%3F%20Jeddict%20AI%20Assistant%20is%20here%20to%20help.%20Autocomplete%2C%20generate%2C%20and%20refactor%20with%20a%20single%20click.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🔍 Discover the power of Jeddict AI Assistant. Autocomplete, generate, and refactor code like never before. Your coding journey just got easier.
                    "https://twitter.com/intent/post?text=%F0%9F%94%8D%20Discover%20the%20power%20of%20Jeddict%20AI%20Assistant.%20Autocomplete%2C%20generate%2C%20and%20refactor%20code%20like%20never%20before.%20Your%20coding%20journey%20just%20got%20easier.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🚀 Jeddict AI Assistant: Your all-in-one solution for intelligent code suggestions, autocompletions, and contextual insights. Try it now!
                    "https://twitter.com/intent/post?text=%F0%9F%9A%80%20Jeddict%20AI%20Assistant%3A%20Your%20all-in-one%20solution%20for%20intelligent%20code%20suggestions%2C%20autocompletions%2C%20and%20contextual%20insights.%20Try%20it%20now!%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 📚 Save reusable prompts with Jeddict’s AI Assistant Settings. No need to retype — just run your saved shortcuts.
                    "https://twitter.com/intent/post?text=%F0%9F%93%9A%20Save%20reusable%20prompts%20with%20Jeddict%E2%80%99s%20AI%20Assistant%20Settings.%20No%20need%20to%20retype%20%E2%80%94%20just%20run%20your%20saved%20shortcuts.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🧩 Tired of writing interfaces, implementations, and service layers? Let Jeddict AI Assistant generate them for you.
                    "https://twitter.com/intent/post?text=%F0%9F%A7%A9%20Tired%20of%20writing%20interfaces%2C%20implementations%2C%20and%20service%20layers%3F%20Let%20Jeddict%20AI%20Assistant%20generate%20them%20for%20you.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🌍 Whether you're a beginner or a seasoned architect, Jeddict AI Assistant makes @netbeans feel like a next-gen IDE.
                    "https://twitter.com/intent/post?text=%F0%9F%8C%8D%20Whether%20you're%20a%20beginner%20or%20a%20seasoned%20architect%2C%20Jeddict%20AI%20Assistant%20makes%20%40netbeans%20feel%20like%20a%20next-gen%20IDE.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 💡 Let AI write your boilerplate. Focus on the why, not the what. Try Jeddict AI Assistant in @netbeans.
                    "https://twitter.com/intent/post?text=%F0%9F%92%A1%20Let%20AI%20write%20your%20boilerplate.%20Focus%20on%20the%20why%2C%20not%20the%20what.%20Try%20Jeddict%20AI%20Assistant%20in%20%40netbeans.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🚀 From file generation to smart refactors — Jeddict AI Assistant turns NetBeans into a powerhouse.
                    "https://twitter.com/intent/post?text=%F0%9F%9A%80%20From%20file%20generation%20to%20smart%20refactors%20%E2%80%94%20Jeddict%20AI%20Assistant%20turns%20%40netbeans%20into%20a%20powerhouse.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🧠 Jeddict AI Assistant understands your code. Autocomplete, generate, and refactor like a pro.
                    "https://twitter.com/intent/post?text=%F0%9F%A7%A0%20Jeddict%20AI%20Assistant%20understands%20your%20code.%20Autocomplete%2C%20generate%2C%20and%20refactor%20like%20a%20pro.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // ✨ Say goodbye to repetitive coding. Jeddict AI Assistant does the heavy lifting — you write logic.
                    "https://twitter.com/intent/post?text=%E2%9C%A8%20Say%20goodbye%20to%20repetitive%20coding.%20Jeddict%20AI%20Assistant%20does%20the%20heavy%20lifting%20%E2%80%94%20you%20write%20logic.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // ✏️ Write code with less typing. Jeddict AI Assistant gets your intent and completes the rest.
                    "https://twitter.com/intent/post?text=%E2%9C%8F%EF%B8%8F%20Write%20code%20with%20less%20typing.%20Jeddict%20AI%20Assistant%20gets%20your%20intent%20and%20completes%20the%20rest.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🔮 Jeddict AI Assistant understands your codebase and suggests intelligent improvements.
                    "https://twitter.com/intent/post?text=%F0%9F%94%AE%20Jeddict%20AI%20Assistant%20understands%20your%20codebase%20and%20suggests%20intelligent%20improvements.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🧠 Jeddict AI Assistant understands your project context to suggest relevant code — directly inside @netbeans. Write less, build more.
                    "https://twitter.com/intent/post?text=%F0%9F%A7%A0%20Jeddict%20AI%20Assistant%20understands%20your%20project%20context%20to%20suggest%20relevant%20code%20%E2%80%94%20directly%20inside%20%40netbeans.%20Write%20less%2C%20build%20more.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 💻 Build Java applications 2x faster with Jeddict AI Assistant — your in-IDE powerhouse for autocompletion, smart refactors, and code generation.
                    "https://twitter.com/intent/post?text=%F0%9F%A7%91%E2%80%8D%F0%9F%92%BB%20Build%20Java%20applications%202x%20faster%20with%20Jeddict%20AI%20Assistant%20%E2%80%94%20your%20in-IDE%20powerhouse%20for%20autocompletion%2C%20smart%20refactors%2C%20and%20code%20generation.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🧠 Jeddict AI Assistant turns ideas into implementation. Right inside @netbeans .
                    "https://twitter.com/intent/post?text=%F0%9F%A7%A0%20Jeddict%20AI%20Assistant%20turns%20ideas%20into%20implementation.%20Right%20inside%20%40netbeans%20.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🧠 Supercharge your @netbeans IDE with AI. Jeddict AI Assistant writes boilerplate, suggests improvements, and speeds up your entire dev flow.
                    "https://twitter.com/intent/post?text=%F0%9F%A7%A0%20Supercharge%20your%20%40netbeans%20IDE%20with%20AI.%20Jeddict%20AI%20Assistant%20writes%20boilerplate%2C%20suggests%20improvements%2C%20and%20speeds%20up%20your%20entire%20dev%20flow.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 💡 Build better and faster — without leaving your @netbeans IDE. Jeddict AI Assistant fits right into your development flow.
                    "https://twitter.com/intent/post?text=%F0%9F%92%A1%20Build%20better%20and%20faster%20%E2%80%94%20without%20leaving%20your%20%40netbeans%20IDE.%20Jeddict%20AI%20Assistant%20fits%20right%20into%20your%20development%20flow.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🔧 Customize prompts, store them, reuse them — Jeddict AI Assistant works like an AI co-developer who remembers everything.
                    "https://twitter.com/intent/post?text=%F0%9F%94%A7%20Customize%20prompts%2C%20store%20them%2C%20reuse%20them%20%E2%80%94%20Jeddict%20AI%20Assistant%20works%20like%20an%20AI%20co-developer%20who%20remembers%20everything.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20",
                    // 🚀 Faster prototyping, cleaner scaffolding, and AI-powered generation — welcome to coding with Jeddict AI Assistant.
                    "https://twitter.com/intent/post?text=%F0%9F%9A%80%20Faster%20prototyping%2C%20cleaner%20scaffolding%2C%20and%20AI-powered%20generation%20%E2%80%94%20welcome%20to%20coding%20with%20Jeddict%20AI%20Assistant.%0A%0A%40ImJeddict%0A%0A&url=%20https%3A%2F%2Fjeddict.github.io%2Fpage.html%3Fl%3Dtutorial%2FAI%20"
            );

    public static String getRandomTweet() {
        int randomIndex = ThreadLocalRandom.current().nextInt(urlList.size());
        return urlList.get(randomIndex);
    }
}
