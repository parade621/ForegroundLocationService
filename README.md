# Foreground Service (Location) Sample Code

Foreground Service 실습을 위한 예제 코드입니다.<br>
본 예제 코드는 위치 데이터를 갱신하는 서비스 코드를 다루고 있습니다.<br>
코드에 대한 설명은 주석으로 나름 열심히 작성해 두었습니다.<br>
사족이 붙은 설명은 Tstory 블로그를 통해 확인 가능합니다.(현재 작성 중)

### 👀 주요 기능

- 실시간 위치 추적
- Foreground Service를 통한 위치 정보 업데이트
- 위치 데이터의 처리 및 활용

### 👀 동작 모습

<img src="https://github.com/parade621/Foreground_Service-Location-_Sample/assets/36446270/2e1ad226-456e-40f0-847e-0effd15ec0b6" width="30%" height="30%">

### 👀 기술 스택

- Kotlin
- Android SDK
- Foreground Service API
- DataBinding
- Timber(Logger)

<br/>

### 👀 구조

앱의 주요 구성 요소는 다음과 같습니다.

- LocationService: 위치 정보를 실시간으로 추적하는 Foreground Service.
- SplashActivity: 앱 시작 시 나타나는 스플래시 화면.
- BasicActivity: 앱의 메인 화면으로, 사용자 인터페이스를 제공합니다.
- ServiceModel: 서비스에서 수행될 비즈니스 로직을 정리하는 클래스.

<br/>

### 👀 앱 동작 순서

1. 앱 시작
    - 사용자가 앱을 실행하면 SplashActivity가 표시됩니다. 이는 앱의 초기 로딩 화면으로, 필요한 리소스를 로드하는 동안 표시됩니다.
2. 메인 화면으로 이동
    - 초기 로딩이 완료되면, 앱은 자동으로 BasicActivity(메인 화면)로 전환됩니다. 이 화면에서 사용자는 앱의 주요 기능(위치 갱신)에 접근할 수 있습니다.
3. 위치 서비스 시작
    - 사용자가 메인 화면에 도달하면 LocationService가 시작됩니다. 이 서비스는 앱이 백그라운드에서도 위치 정보를 계속 추적할 수 있도록 합니다.
4. 실시간 위치 추적
    - LocationService는 사용자의 현재 위치를 실시간으로 추적하고 업데이트합니다.
5. 위치 정보 표시
    - BasicActivity는 LocationService로부터 받은 위치 데이터를 화면에 표시합니다. 사용자는 자신의 현재 위치 정보를 Refresh 버튼을 통해 갱신할
      수 있습니다.
6. 서비스 종료
    - 사용자가 위치 서비스를 종료하는 버튼을 클릭하거나 앱을 종료하면, LocationService는 자동으로 중단됩니다. 이때, 모든 위치 추적 작업이 종료되고, 서비스는
      자원을 해제합니다.

<br/>

### 👀 사족

- 본 예제는 가장 간단하게 Foreground Service를 설명하려고 만든 예제로, Flow나 Coroutine과 같은 비동기 처리는 적용하지 않았습니다.
- 따라서, 현재 예제는 업데이트된 위치 정보를 실시간으로 View에 그리지 않으며, 버튼 클릭을 통해 갱신하는 방식을 선택하였습니다.
- 실시간 위치 갱신이 필요하다면, 비동기 처리를 반드시 고려해야합니다.
- 배터리 최적화를 해지하는 코드가 추가되었습니다. Foreground Service를 사용할 때, 배터리 최적화를 해제해야 서비스가 정상적으로 동작하는 경우가 있기 떄문에 추가해 두었습니다.

<br/>

더 자세한 위치 정보 서비스 코드를 보고
싶으시다면 [https://github.com/android/location-samples](https://github.com/android/location-samples) 해당
링크를 참조 부탁드립니다.

예제에 관련된 질문이 있거나 오류가 있다면 Issue를 남겨주시면 감사하겠습니다.

