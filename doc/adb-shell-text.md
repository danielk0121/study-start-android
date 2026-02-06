### 1. adb 명령어 위치 찾기 및 설정

백엔드 개발자라면 `adb`를 터미널에서 바로 쓰는 게 훨씬 편하실 겁니다. `adb`는 안드로이드 SDK 폴더 안의 `platform-tools` 디렉토리에 숨어 있습니다.

**경로 확인법:**
1. 인텔리제이 설정(`Cmd + ,`) -> **Appearance & Behavior > System Settings > Android SDK**로 이동합니다.
2. 상단에 **Android SDK Location** 경로를 복사하세요. (보통 `/Users/계정명/Library/Android/sdk`입니다.)
3. 터미널에서 해당 경로 뒤에 `/platform-tools/adb`를 붙이면 실행됩니다.

**터미널 어디서든 쓰게 설정하기 (zsh 기준):**
~
# 1. zsh 설정 파일 열기
nano ~/.zshrc

# 2. 맨 아래에 아래 내용 추가 (사용자명 부분은 본인 계정명으로 수정)
export PATH=$PATH:/Users/사용자명/Library/Android/sdk/platform-tools

# 3. 저장(Ctrl+O, Enter) 후 나가기(Ctrl+X)
# 4. 설정 적용
source ~/.zshrc

# 5. 확인
adb version
~

이제 터미널에서 `adb shell input text "내용"`을 치면 에뮬레이터에 바로 입력됩니다! (단, 띄어쓰기가 있는 문자열은 `" "`로 감싸주세요.)

