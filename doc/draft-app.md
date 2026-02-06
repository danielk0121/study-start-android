---

### 1. 요구사항 명세 목록 (Requirements Specification)

**[서비스 개요]**
안드로이드 앱에서 메모를 작성하고 GitHub 레포지토리에 파일 형태로 푸시(Push)하는 개인용 메모장 앱.

**[기능 요구사항]**
1.  **메모 작성:** 제목과 내용을 입력할 수 있는 필드 제공.
2.  **GitHub 연동:** * GitHub REST API를 사용하여 파일 생성/조회/수정 구현.
    * 메모 저장 시 지정된 레포지토리의 경로에 파일로 커밋.
3.  **메모 관리:**
    * 기존 메모 목록 및 내용 불러오기 (GET API).
    * 불러온 메모 수정 후 업데이트 (PUT API + SHA 체크).
4.  **설정:** 사용자가 자신의 GitHub PAT(Personal Access Token)를 직접 입력하고 저장하는 기능.

**[비기능 및 보안 요구사항]**
1.  **배포 방식:** Play 스토어 미등록, APK 직접 빌드 및 설치.
2.  **보안 (핵심):** * PAT는 코드에 하드코딩하지 않음.
    * 사용자가 입력한 토큰은 `EncryptedSharedPreferences`를 통해 암호화하여 기기 내부에 저장.
    * 네트워크 통신은 HTTPS(TLS)를 필수 적용.
3.  **사용성:** 최소한의 UI 컴포넌트(EditText, Button)를 활용한 직관적인 구성.

---

### 2. To-Do Task 목록 (Development Tasks)

**Step 1: 환경 구축 및 프로젝트 설정**
- [ ] 안드로이드 스튜디오 프로젝트 생성 (Kotlin 권장)
- [ ] 의존성 라이브러리 추가 (`Retrofit2`, `OkHttp`, `Security-crypto`, `ViewBinding` 등)
- [ ] `AndroidManifest.xml`에 인터넷 권한 추가

**Step 2: 보안 및 설정 기능 구현**
- [ ] `EncryptedSharedPreferences`를 활용한 토큰 저장/불러오기 유틸리티 클래스 작성
- [ ] PAT 입력을 위한 설정(Setting) 화면 구성
- [ ] 앱 시작 시 토큰 존재 여부 체크 로직 구현

**Step 3: GitHub API 인터페이스 정의 (Retrofit)**
- [ ] GitHub API Base URL 설정 (`https://api.github.com/`)
- [ ] 파일 조회(GET), 생성 및 수정(PUT) 인터페이스 함수 정의
- [ ] OkHttp Interceptor를 만들어 모든 요청 헤더에 `Authorization: token {PAT}` 자동 삽입

**Step 4: UI 및 비즈니스 로직 개발**
- [ ] **메인 화면:** 메모 목록 표시 (선택 사항) 또는 최근 파일 불러오기
- [ ] **작성 화면:** `EditText`(제목, 내용) 및 `저장` 버튼 배치
- [ ] **수정 로직:** 기존 파일 조회 시 받은 `sha` 값을 변수에 보관했다가 업데이트 시 전송하는 로직 구현
- [ ] **인코딩 처리:** 텍스트를 Base64로 변환하여 API 바디에 담기

**Step 5: 테스트 및 배포**
- [ ] 디버그 로그에 토큰이 찍히지 않는지 확인
- [ ] 실제 GitHub 레포지토리에 파일이 정상적으로 커밋되는지 테스트
- [ ] Release 버전 APK 빌드 및 스마트폰 설치 테스트
