## 오목 백엔드 서버 프로젝트

- SpringBoot 3.2.6 버전을 사용하여 오목게임의 백엔드 서버를 구현한 프로젝트입니다.
- 전체적으로 단기적인 데이터만 취급하므로, Redis를 주 DB로 사용하여 서버를 구성합니다.
- 인증 JWT를 통해 정적인 인증 방식을 수행하며, SpringSecurity를 사용하여 인증 역할을 제어합니다.


---

## 오목 서버의 역할

### 게임방 목록 보여주기

- 아직 플레이어를 대기하고 있는 게임 방 목록을 보여줍니다.

### 게임방 만들기
### 게임방 입장하기
### 오목돌 두기
### 게임 규칙 밸리데이션 및 승리 판정

- 오목의 규칙에 맞게 돌을 두었는지 유효성 검증(validation)을 수행합니다.
- 돌을 둔 이후에 어떤 이가 승리하였는지 여부를 판단합니다.

### 블로그 포스팅

- [[유니티 + 스프링] 오목게임을 개발하자(1) - 설계 및 Figma로 UI구성](https://blog.naver.com/jhsfully/223451486323)
- [[유니티 + 스프링] 오목게임을 개발하자(2) - Unity로 메인UI 구성](https://blog.naver.com/jhsfully/223451538892)
- [[유니티 + 스프링] 오목게임을 개발하자(3) - Unity로 모달창 구성](https://blog.naver.com/jhsfully/223465871156)
- [[유니티 + 스프링] 오목게임을 개발하자(4) - Unity에서 모서리 Round 처리하기](https://blog.naver.com/jhsfully/223480877654)
- [[유니티 + 스프링] 오목게임을 개발하자(5) - 백엔드 서버 개발](https://blog.naver.com/jhsfully/223504380232)
- [[유니티 + 스프링] 오목게임을 개발하자(6) - 메인 화면 연동](https://blog.naver.com/jhsfully/223551307962)
- [[유니티 + 스프링] 오목게임을 개발하자(7) - 인게임 구성 및 연동](https://blog.naver.com/jhsfully/223551343709)

---

## 사용한 라이브러리

|       라이브러리       |             버전             |
|:-----------------:|:--------------------------:|
|    SpringBoot     |           3.2.6            |
| spring-data-redis | (dependency management 의존) |
| spring-security |         (dependency menagement 의존) |
| spring-validation | (dependency menagement 의존) |
| spring-web | (dependency menagement 의존) |
| lombok | (dependency menagement 의존) |
| jjwt | 0.12.5 |
