# Auto-generated by EclipseNSIS Script Wizard
# 02.08.2011 13:26:22

Name Jeboorker

# General Symbol Definitions
!define REGKEY "SOFTWARE\$(^Name)"
!define COMPANY ""
!define URL ""

# MUI Symbol Definitions
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install-colorful.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY ${REGKEY}
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULTFOLDER Jeboorker
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall-colorful.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE

# Included files
!include Sections.nsh
!include MUI2.nsh
!include TextFunc.nsh

# Variables
Var StartMenuGroup

# Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE German

# Installer attributes
OutFile "target\jeboorker_${VERSION}.exe"
InstallDir $PROGRAMFILES\Jeboorker
CRCCheck on
XPStyle on
ShowInstDetails hide
VIProductVersion 0.4.4.0
VIAddVersionKey ProductName Jeboorker
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey FileVersion "${VERSION}"
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

# Installer sections
Section -Main SEC0000
    SetOverwrite on

	SetOutPath $INSTDIR
    File dist\Jeboorker.bat
    File dist\Jeboorker.vbs
    File dist\win32\Jeboorker.exe
    File Readme.txt

    SetOutPath $INSTDIR\doc\license
    File /r doc\license\*

    SetOutPath $INSTDIR\exec
    File 'dist\win32\*'

    RMDir /r $INSTDIR\lib
    SetOutPath $INSTDIR\lib
    File commons\target\*.jar
    File commons\target\lib\*.jar
    File commons-swing\target\*.jar
    File commons-swing\target\lib\*.jar
    File epublib\target\*.jar
    File epublib\target\lib\*.jar
    File jpushbullet\target\*.jar
    File jpushbullet\target\lib\*.jar
    File l2fprod\target\*.jar
    File l2fprod\target\lib\*.jar
    File myswing\target\*.jar
    File myswing\target\lib\*.jar
    File jeboorker-main\target\*.jar
    File jeboorker-main\target\lib\*.jar
    File jeboorker-main\lib\*.jar
    File jeboorker-main\lib\*.dll

    WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

Section -post SEC0001
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    SetOutPath $SMPROGRAMS\$StartMenuGroup
#    CreateShortCut "$SMPROGRAMS\$StartMenuGroup\Jeboorker for Java 32bit.lnk" "$INSTDIR\Jeboorker32.exe"
#    CreateShortCut "$SMPROGRAMS\$StartMenuGroup\Jeboorker for Java 64bit.lnk" "$INSTDIR\Jeboorker64.exe"
    SetOutPath $INSTDIR
    CreateShortCut "$SMPROGRAMS\$StartMenuGroup\Jeboorker.lnk" "$INSTDIR\Jeboorker.exe"

    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk" $INSTDIR\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_END
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o -un.Main UNSEC0000
    RMDir $INSTDIR
    DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

Section -un.post UNSEC0001
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk"
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /R /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /R /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function .onInit
    InitPluginsDir
FunctionEnd

# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuGroup
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd


