# README: Рускоязычная секция
For English README access click ["English section"](#readme-english-section)
## Что такое umOS::Snippets?
umOS::Snippets - небольшая библиотека поддержки ассинхронного кода в Java проектах с целевым JRE 8 и выше, а также Android OS. Основное назначение - упростить ассинхронное выполенение кода и контроль за ним, через описание сниппетов - концепцию изолированных фрагментов кода. Создана для личного пользования в некоторых проектах. Возможно кому-нибудь поможет.

# Новвоведения от 2024/09/20
Промежуточное обновление кодовой базы umOS::Snippets.
```
* Использование Java JRE 8 и выше
* Интеграция Methods Reference, Lambdas в launch*(...)
* [BCHANGES] Изменение в определении Snippet - теперь это функциональный интерфейс
* [BCHANGES] Изменение в семействе функций Threads.launch*(...), теперь необходимо явно указывать политику потока для запуска сниппета
* Изменение в Example - для совместимости с новыми возможностями
```

Таким образом, промежуточное обновление затрагивает объявление сниппета, а также функцию запуска данных сниппетов. Теперь есть возможность использовать лямбда-выражения и ссылки на методы.

Раньше:
```
Snippet todo_smth = new Snippet({TARGET_POLICY}) {
		@Override public void todo(Matcher m) {
			//Write your async code here
	}
};
```

Сейчас:
```
Snippet todo_smth = new Snippet() {
		@Override public void todo(Matcher m) {
			//Write your isolated code here
	}
};

ИЛИ

Snippet todo_smth = (m) -> { 
	//Write your isolated code here
};

ИЛИ

launch({THREAD_POLICY}, this::methodAsTypeSnippet, ...);

ИЛИ

launch*(this_or_static_class::methodAsTypeSnippet, ...);

```

Теперь Threads.launch*() может принимать различные аргументы IORC-профиля. Это означает что необходимо лишь соблюдать последовательность объектов IORC, например,  сперва идет входные данные, потом если не нужны выходные данные, то можно передавать лаунчеру Reaction и Cancellable (опционально).

Так же были интегрированы следующие функции запуска, чтобы сократить команду запуска:
```
launchNow(snippet, ... iorc) - запустить сниппет в доступном Intantable-провайдере

launchUI(snippet, ... iorc) - запустить сниппет в доступном UI-провайдере

launchStack(snippet, ... iorc) - запустить сниппет в доступном Stack-провайдере
```

### Поддерживаемые варианты последовательности IORC-профиля
Допустим вы вызываете launch()/launchNow()/launchUI()/launchStack() передавая сниппет и некоторую последовательность IORC-профиля, в следующей таблице указаны соответсвие числу аргументов после сниппета и поддерживаемый такому числу вариант IORC-профиля:

|Число аргументов|Возможные варианты IORC - последовательности |
|-|-|
|Snippet+null| throws RuntimeException|
|Snippet+0 (only snippet)| исполнение данного сниппета, передаваемый IORC-профиль из провайдера потока|
|Snipept+1|Или I(input) объект данных или Reaction, остальное - представление провайдером потока|
|Snipept+2|Возможные вариации профилей RC, IR, IO, остальные данные - провайдером потока|
|Snipept+3|Возможные вариации профилей IRC, IOR, остальные данные - провайдером потока|
|Snipept+4|Полноценный IORC-профиль, представленный вами|


# README для PB20240726/1G0
## Основные объекты
При  работе с библиотекой требуется изучить следующие объекты.
### Threads - лаунчер сниппетов
Класс *Threads* отвечает за выполнение сниппетов, избавляя вас от обязанностей поиска нужного потока. Передаваемый ему сниппет самостоятельно найдет нужный поток.  Перед запуском сниппетов вам необходимо подключить ThreadManager, однако библиотека стандартно поставляет готовый менеджер потоков - DefaultManager, который реализует стаковое выполнение сниппетов, а также параллельное. Для использования:
```
final DefaultManager defmanager = new DefaultManager();
//OR
final DefaultManager defmanager = new DefaultManager(new SmthThreadProviderUIJOBS());

Threads.link(defmanager);
//Preparing app stage ended

...
//Smthing event trigger async-behaviour
	Threads.launch(..., your_snippet);
...
//When you Terminating APP - destroy all!
Threads.unlink();
```
Учтите, что DefaultManager не предоставляет провайдера для политики THREAD_UIABLE, так как выполнение кода в GUI потоке зависит от целевой платформы, поэтому реализация данного провайдера остается на стороне разработчика. Смотрите код в snippets-examples для элементарной реализации "in UI-thread" в Java SWING.
> Так же разработчиком могут быть реализованы собственные менеджеры и провайдеры потоков для достижения нужного поведения. Однако Threads не знает об их существовании, уведомите DefaulManager или своего менеджера о новом провайдере через add()-begin() - методы на необходимом этапе жизенного цикла вашего приложения.

В Threads определены следующие политики: THREAD_STACKABLE - для выполнения асинхронного пакетного выполнения; THREAD_INSTANABLE - мгновенное выполнение в отдельном потоке; THREAD_UIABLE - политика для потоков выполняющих код в UI-thread, реализуется для конкретных платформ самостоятельно.
### Snippet - фрагмент кода
В сниппетах вы пишете код который должен быть выполнен асинхронно, но может быть вызван повторно (как метод Java).  Чтобы определить сниппет необходимо создать объект Snippet в любом месте Вашего кода:
```
Snippet todo_smth = new Snippet({TARGET_POLICY}) {
		@Override public void todo(Matcher m) {
			//Write your async code here
	}
};
```
*{TARGET_POLICY}* - преследуемая политика потоков, в каком из потоков сниппет будет выполняться, если вы используете собственный провайдер потока для специфичного поведения, то указывайте в сниппете его политику потока.
Сниппеты унаследованы от Runnables, поэтому можно использовать  сырой вызов напрямую:
```
todo_smth.match(Smth_Matcher_Object); //optional line
new Thread(todo_smth).start();
```
Только если вашему сниппету нужны объекты для взаимодействия, заранее сопоставьте сниппету его Matcher, предоставляющий IORC-профиль (комментарий - optional line). **Однако учтите,** что вызов match() может порождать утечки памяти, так как ссылка на ненужный IORC-профиль будет удержана в данном сниппете, пока не будет совершен повторный вызов match(...). 
### Matcher, IORC-профиль
Сниппеты должны общаться с внешним кодом только через  свой IORC-профиль, предоставляемый Matcher-объектом в *todo()*-методе. При нормальном запуске *Threads.launch(..., todo_smth)* лаунчер автоматически подготовит запрашиваемый IORC-профиль, в зависимости от входных параметров.
Matcher предоставляет следующие методы:
- *in()* - получить входной объект, нужный для работы. В зависимости от условий запуска может быть null, тип - Object;
- *out()* - получить выходной объект, например, куда будут записаны данные, может также использоваться для хранения дополнительных параметров, В зависимости от условий запуска может быть null, тип - Object;
- reaction() - получить каллбэк для реакции во время работы сниппета, не может быть null, тип - Reaction, подробнее смотрите в соответствующем параграфе;
- *cancelation()* - получить каллбэк, позволяющий узнавать заинтересованность в продолжении работы. Необходимо использовать его если, сниппет выполняет код с возможностью "отмены во время выполнения". Не может быть null, тип - Cancellation, подробнее смотрите в соответствующем параграфе;
- *juid()* - универсальный идентификатор задачи исполняемого сниппета. Не может быть null, тип - String.

Сниппеты никогда не должны общаться с внешним кодом, только через IORC-профиль, однако допустимо обращение к внешним полям, если сниппет направлен на работу с UI-компонентами.
### Reaction
Интерфейс Reaction позволяет анонимизировать общение между классами, чтобы сузить разнообразие общений между ними. Класс реализующий Reaction должен уметь реагировать на следующие методы:
- *begin()* - кто-то извещает вас о начале какой-либо процедуры, как правило это вызывается в сниппетах, чтобы вы знали, что будет меняться модель данных, и должны например заблокировать доступ к интерактивным элементам позволяющим пользователю влиять на эту модель данных - возможность избежать ошибок с мультипоточной мутацией данных;
- *end()* - кто-то извещает вас о том, что процедура завершена. Можно возвращать доступ к интерактивным элементам пользователю;
- *progress(String juid, float value)* - Задача с уникальным идентификатором JUID оповещает вас, что она выполнена на value, значение может быть произвольным, определяется самостоятельно разработчиком к собственных сниппетах;
- *error(String juid, String etag, Object packet)* -  Задача с уникальным идентификатором JUID оповещает вас, что произошла ошибка с тегом "etag" и передает вам некоторый объект для самостоятельного анализа;
- *event(String tag, Object packet)* - кто-то просит вас среагировать на событие помеченное тегом tag и передающее объект packet для самостоятельного анализа.

>Reaction имеет и другие методы *in(), out(), read(), write()*, однако они нужны лишь для более гибкого взаимодействия с анонимизированными классами.
### Cancellable - асинхронно отменяемое выполнение

Интерфейс Cancellable позволяет вашему классу анонимно описывать свое состояние отмены задач. Иначе говоря, например, если пользователь нажал клавишу отмена, то он изменил состояние некоторого класса, реализующего cancellable-методы:
- *doing(String juid)* - кто-то хочет узнать у вас, остались ли элементы заинтересованные в выполнении текущей задачи с уникальным идентификатором JUID?: true - если задача juid еще интересна, false - если задача juid никому не нужна.
- *cancel(String juid, boolean state)* - кто-то попросил вас отменить/зарегистрировать интерес в задаче juid. Поэтому будьте готовы - ваш класс в скором времени могут опрашивать на *doing(...)*.

> Cancellable необходим для возможности сниппету досрочно прекратить свою работу, если в приложении больше не осталось никого, кто был бы заинтересован в его выполнении с текущим IORC-профилем.
> Например, выполняемый сниппетом код загрузки большого файла из интернета можно прервать, если организовать условие прерывания по doing(juid) в циклах for(), while(), ключевых шагах сниппета и т.д. - true - кому-то нужен результат работы сниппета, false - никого из заинтересованных не осталось, можно не завершать работу, не забудьте почистить и освободить занятые ресурсы.
> Однако логика прерывания сниппета - сугубо задача разработчика сниппета. Cancellable лишь должен отвечать, что нужно продолжать работу, или её можно прервать.

## Threads.launch(..., snippet)
Лаунчер сниппетов может запускать их по разному. Например, если вашему сниппету безразлично состояние прерывания, однако нужны входные и выходные объекты, а так же реакция,  то подойдет следующий launch():
```
Threads.launch(in_object, out_object, reaction_callback, your_snippet);
```
Или ваш сниппет требует наличие проверки прерывания, то:
```
Threads.launch(reaction_callback, cancel_state, your_snippet);
```
Или выполнить только сниппет для модификации UI-элементов (необходимость в Android OS):
```
Threads.launch(your_snippet);
```
И т.д.
>Независимо от параметров запуска Threads.launch(..)  всегда формирует IORC-профиль, если вашему сниппету неинтересны I/O-объекты, Reaction/Cancellable состояние, то в профиле Matcher они будут ссылаться на провайдера потока, указанный при создании сниппета.
> Тогда IORC-поведение будет предоставляться конкретной реализацией провайдера потока, что зависит от разработчика.
> DefaultManager автоматически реализует два провайдера InstantJob и StackableJob. Первый не поддерживает "прерывание во время выполнения", второй поддерживает. I/O-объекты - всегда null.

## Примеры кода
В папке snippet-examples приводится пример решения асинхронных задач с использованием сниппетов. Приложение реализует загрузку коубов с сайта coub.com (предоставлено исключительно в учебных целях). Так же вы можете найти там пример элементарного провайдера для политики THREAD_UIABLE (класс ru.manku.desktop.SwingJob), регистрацию и сопровождение жизненного цикла Threads. Для корректной работы примера потребуется бинарный исполняемый файл ffmpeg-библиотеки.


# README: English section
Translated by Google Translate

# New changes from 2024/09/20
Interim update of the umOS codebase::Snippets.
```
* Using Java JRE 8 and above
* Integration of Methods Reference, Lambdas into launch*(...)
* [BCHANGES] Change in the definition of Snippet - now it is a functional interface
* [BCHANGES] A change in the Threads.launch*(...) family of functions, it is now necessary to explicitly specify the thread policy for launching the snippet
* Change in Example - for compatibility with new features
```

Thus, the interim update affects the announcement of the snippet, as well as the function of launching these snippets. Now it is possible to use lambda expressions and method references.

Before:
```
Snippet todo_smth = new Snippet({TARGET_POLICY}) {
	@Override public void todo(Matcher m) {
		//Write your async code here
	}
};
```

Now:
```
Snippet todo_smth = new Snippet() {
	@Override public void todo(Matcher m) { 
		//Write your isolated code here
	}
};

OR 

Snippet todo_smth = (m) -> { //Write your isolated code here };

OR

launch({THREAD_POLICY}, this::methodAsTypeSnippet, ...);

OR

launch*(this_or_static_class::methodAsTypeSnippet, ...);
```

 
# README for PB20240726/1G0
## What are umOS::Snippets?
umOS::Snippets is a small library for supporting asynchronous code in Java projects with a target JRE 8 and higher, as well as Android OS. The main purpose is temporary asynchronous execution of code and control over it, through the description of snippets - the inclusion of isolated code fragments. Created for personal use in some projects. Perhaps it will help someone.
## Main objects
When working with the library, you need to study the following objects.
### Threads - snippet launcher
The *Threads* class is responsible for executing snippets, relieving you of the burden of finding the right thread. The snippet passed to it will automatically find the required thread.  Before running snippets, you need to connect the ThreadManager, however, the library standardly supplies a ready-made thread manager - DefaultManager, which implements stacked and parallel execution of snippets. For use:
```
final DefaultManager defmanager = new DefaultManager();
//OR
final DefaultManager defmanager = new DefaultManager(new SmthThreadProviderUIJOBS());

Threads.link(defmanager);
//Preparing app stage ended

...
//Smthing event trigger async-behaviour
	Threads.launch(..., your_snippet);
...
//When you Terminating APP - destroy all!
Threads.unlink();
```
Please note that DefaultManager does not provide a provider for the THREAD_UIABLE policy, since code execution in the GUI thread depends on the target platform, so the implementation of this provider is left to the developer. See the code in snippets-examples for a rudimentary implementation of "in UI-thread" in Java SWING.
> The developer can also implement his own thread managers and providers to achieve the desired behavior. However, Threads does not know about their existence, notify DefaulManager or your manager about the new provider via add()-begin() methods at the appropriate stage in your application's lifecycle and dont *forget Threads.link(your_manager).*

The following policies are defined in Threads: THREAD_STACKABLE - for performing asynchronous batch execution; THREAD_INSTANABLE - instant execution in a separate thread; THREAD_UIABLE - policy for threads executing code in the UI-thread, implemented independently for specific platforms.
### Snippet - code fragment
In snippets, you write code that must be executed asynchronously, but can be called repeatedly (like a Java method).  To define a snippet you need to create a Snippet object anywhere in your code:
```
Snippet todo_smth = new Snippet({TARGET_POLICY}) {
		@Override public void todo(Matcher m) {
			//Write your async code here
	}
};
```
*{TARGET_POLICY}* - the thread policy being pursued, in which thread the snippet will be executed. If you use your own thread provider for specific behavior, then indicate its thread policy in the snippet.
Snippets are inherited from Runnables, so you can use the raw call directly:
```
todo_smth.match(Smth_Matcher_Object); //optional line
new Thread(todo_smth).start();
```
Only if your snippet needs objects to interact with, match the snippet in advance with its Matcher, which provides an IORC profile (comment - optional line). **However, please note** that calling match() may cause memory leaks, since the reference to the unnecessary IORC profile will be held in this snippet until the match(...) call is repeated.
### Matcher, IORC profile
Snippets should communicate with external code only through their IORC profile provided by the Matcher object in the *todo()* method. During normal startup *Threads.launch(..., todo_smth)* the launcher will automatically prepare the requested IORC profile, depending on the input parameters.
Matcher provides the following methods:
- *in()* - get the input object needed for the job. Depending on the launch conditions, it can be null, the type is Object;
- *out()* - get an output object, for example, where data will be written, it can also be used to store additional parameters, Depending on the startup conditions, it can be null, type - Object;
- reaction() - get a callback for a reaction while the snippet is running, it cannot be null, the type is Reaction, for more information see the corresponding paragraph;
- *cancellation()* - get a callback that allows you to find out your interest in continuing work. You must use it if the snippet executes code with the ability to "cancel at runtime". It cannot be null, the type is Cancellation, for more information, see the corresponding paragraph;
- *juid()* is the universal task identifier of the executed snippet. It cannot be null, the type is String.

Snippets should never communicate with external code, only through the IORC profile, however, it is acceptable to access external fields if the snippet is aimed at working with UI components.
### Reaction
The Reaction interface allows you to anonymize communication between classes in order to narrow down the variety of communication between them. The class implementing Reaction should be able to respond to the following methods:
- *begin()* - someone notifies you about the beginning of a procedure, as a rule it is called in snippets so that you know that the data model will change, and should, for example, block access to interactive elements that allow the user to influence this model data - the ability to avoid errors with multithreaded data mutation;
- *end()* - someone notifies you that the procedure is completed. You can return access to interactive elements to the user;
- *progress(String juid, float value)* - A task with a unique JUID identifier notifies you that it has been completed on value, the value can be arbitrary, determined independently by the developer to their own snippets;
- *error(String juid, String etag, Object packet)* - A task with a unique JUID identifier notifies you that an error has occurred with the tag "etag" and passes you some object for self-analysis;
- *event(String tag, Object packet)* - someone asks you to react to an event tagged with a tag and passing a packet object for self-analysis.

>Reaction has other methods *in(), out(), read(), write()*, but they are only needed for more flexible interaction with anonymized classes.
### Cancellable - asynchronously cancellable execution

The Cancellable interface allows your class to anonymously describe its task cancellation status. In other words, for example, if the user pressed the cancel key, then he changed the state of some class implementing cancellable methods:
- *doing(String juid)* - someone wants to ask you if there are any elements interested in completing the current task with a unique JUID identifier?: true - if the juid task is still interesting, false - if the juid task is not needed by anyone.
- *cancel(String juid, boolean state)* - someone asked you to cancel/register an interest in the juid task. Therefore, be prepared - your class may soon be interviewed for *doing(...)*.

 Cancellable is necessary to allow the snippet to terminate its work prematurely if there is no one left in the application who would be interested in executing it with the current IORC profile.
> For example, the code executed by the snippet for downloading a large file from the Internet can be interrupted if you organize the interrupt condition by doing(juid) in for(), while() cycles, key steps of the snippet, etc. - true - someone needs the result of the snippet, false - there is no one left interested, you can not complete Please do not forget to clean up and free up occupied resources.
> However, the logic of snippet interruption is purely the task of the snippet developer. Cancellable only has to answer that it is necessary to continue working, or it can be interrupted.

## Threads.launch(..., snippet)
The snippet launcher can launch them in different ways. For example, if your snippet doesn't care about the interrupt state, but needs input and output objects, as well as a reaction, then the following launch() will do:
```
Threads.launch(in_object, out_object, reaction_callback, your_snippet);
```
Or your snippet requires an interrupt check, then:
```
Threads.launch(reaction_callback, cancel_state, your_snippet);
```
Or perform only a snippet to modify UI elements (a need for Android OS):
```
Threads.launch(your_snippet);
```
etc.
>Regardless of the launch parameters, Threads.launch(..) always generates an IORC profile, if your snippet is not interested in I/O objects, Reaction/Cancellable state, then in the Matcher profile they will refer to the stream provider specified when creating the snippet.
> Then the IORC behavior will be provided by the specific implementation of the thread provider, which depends on the developer.
> DefaultManager automatically implements two providers InstantJob and StackableJob. The first one does not support "interrupt during execution", the second one does. I/O objects are always null.

## Code examples
The "snippet-examples" folder contains an example of solving asynchronous tasks using snippets. The application implements the download of coubes from the site coub.com (provided for educational purposes only). You can also find there an example of an elementary provider for the THREAD_UIABLE policy (ru.manku.desktop class.SwingJob), registration and maintenance of the Threads lifecycle. For the example to work correctly, you will need a binary executable file of the ffmpeg library.
