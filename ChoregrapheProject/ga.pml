<?xml version="1.0" encoding="UTF-8" ?>
<Package name="ZoraGA" format_version="4">
    <Manifest src="manifest.xml" />
    <BehaviorDescriptions>
        <BehaviorDescription name="behavior" src="behavior_1" xar="behavior.xar" />
    </BehaviorDescriptions>
    <Dialogs>
        <Dialog name="bingoCommands&#x0D;" src="bingoCommands/bingoCommands.dlg" />
        <Dialog name="ExampleDialog" src="ExampleDialog/ExampleDialog.dlg" />
        <Dialog name="stt" src="stt/stt.dlg" />
        <Dialog name="chooseAction" src="chooseAction/chooseAction.dlg" />
        <Dialog name="listening" src="listening/listening.dlg" />
    </Dialogs>
    <Resources>
        <File name="google-assistant" src="google-assistant.png" />
        <File name="" src=".gitignore" />
        <File name="LICENSE" src="LICENSE" />
        <File name="README" src="README.md" />
        <File name="ZoraThinking" src="sounds/ZoraThinking.wav" />
        <File name="beep" src="sounds/beep.wav" />
    </Resources>
    <Topics>
        <Topic name="bingoCommands_enu" src="bingoCommands/bingoCommands_enu.top" topicName="bingoCommands" language="en_US" />
        <Topic name="ExampleDialog_enu" src="ExampleDialog/ExampleDialog_enu.top" topicName="ExampleDialog" language="en_US" />
        <Topic name="stt_enu" src="stt/stt_enu.top" topicName="stt" language="en_US" />
        <Topic name="chooseAction_enu" src="chooseAction/chooseAction_enu.top" topicName="chooseAction" language="en_US" />
        <Topic name="listening_enu" src="listening/listening_enu.top" topicName="listening" language="en_US" />
    </Topics>
    <IgnoredPaths />
</Package>
