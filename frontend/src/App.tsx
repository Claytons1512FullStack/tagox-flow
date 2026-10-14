import { useCallback, useState, type FormEvent } from "react";
import PatientsPage from "./features/patients/pages/PatientsPage";
import {
  autenticar,
} from "./features/auth/services/authService";
import {
  clearSession,
  getToken,
  saveSession,
} from "./features/auth/services/sessionService";
import "./App.css";

type Page = "dashboard" | "patients";

const CURRENT_YEAR = new Date().getFullYear();

function App() {
  const [token, setToken] = useState(() => getToken());
  const [pagina, setPagina] = useState<Page>("dashboard");

  const [tenantSlug, setTenantSlug] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState("");

  const handleUnauthorized = useCallback(() => {
    clearSession();

    setToken("");
    setPagina("dashboard");
    setErro("Sua sessão expirou. Entre novamente.");
  }, []);

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro("");
    setCarregando(true);

    try {
      const data = await autenticar({
        tenantSlug: tenantSlug.trim(),
        email: email.trim(),
        senha,
      });

      saveSession(data.token, data.tokenType);

      setToken(data.token);
      setSenha("");
      setPagina("dashboard");
      setErro("");
    } catch (error) {
      if (error instanceof TypeError) {
        setErro(
          "Não foi possível conectar ao servidor. Verifique se o backend está funcionando.",
        );
      } else {
        setErro(
          error instanceof Error
            ? error.message
            : "Ocorreu um erro inesperado. Tente novamente.",
        );
      }
    } finally {
      setCarregando(false);
    }
  }

  function handleLogout() {
    clearSession();

    setToken("");
    setPagina("dashboard");
    setTenantSlug("");
    setEmail("");
    setSenha("");
    setErro("");
  }

  if (token) {
    return (
      <main className="app-shell">
        {" "}
        <aside className="app-sidebar">
          {" "}
          <header className="brand app-brand">
            {" "}
            <div className="brand-symbol" aria-hidden="true">
              T{" "}
            </div>
            <div className="brand-text">
              <span className="brand-name">
                TAGOX<span>FLOW</span>
              </span>
              <span className="brand-caption">GESTÃO INTELIGENTE</span>
            </div>
          </header>

          <div className="sidebar-section-label">MENU PRINCIPAL</div>

          <nav className="app-navigation" aria-label="Menu principal">
            <button
              type="button"
              className={`nav-item ${pagina === "dashboard" ? "active" : ""}`}
              onClick={() => setPagina("dashboard")}
            >
              <span className="nav-icon" aria-hidden="true">
                ◫
              </span>
              Visão geral
            </button>

            <button
              type="button"
              className={`nav-item ${pagina === "patients" ? "active" : ""}`}
              onClick={() => setPagina("patients")}
            >
              <span className="nav-icon" aria-hidden="true">
                ♙
              </span>
              Pacientes
            </button>
          </nav>

          <div className="sidebar-bottom">
            <div className="sidebar-status">
              <span className="status-dot" />
              Sessão iniciada
            </div>

            <button
              type="button"
              className="logout-button"
              onClick={handleLogout}
            >
              <span aria-hidden="true">↪</span>
              Sair da conta
            </button>
          </div>
        </aside>

        <section className="app-main">
          <header className="app-topbar">
            <div>
              <span className="topbar-eyebrow">TAGOX FLOW</span>
              <h1>{pagina === "dashboard" ? "Visão geral" : "Pacientes"}</h1>
            </div>

            <div className="account-indicator">
              <span className="account-avatar" aria-hidden="true">
                T
              </span>
              <div>
                <strong>Área de trabalho</strong>
                <span>Conta autenticada</span>
              </div>
            </div>
          </header>

          {pagina === "dashboard" ? (
            <div className="dashboard-content">
              <section className="dashboard-welcome">
                <span className="eyebrow">SEU TRABALHO, CONECTADO</span>
                <h2>Bem-vindo ao TAGOX Flow.</h2>
                <p>
                  Organize sua rotina profissional em um ambiente integrado,
                  seguro e preparado para evoluir com sua empresa.
                </p>
              </section>

              <section className="dashboard-section">
                <div className="section-heading">
                  <div>
                    <span className="topbar-eyebrow">ACESSO RÁPIDO</span>
                    <h2>Seus módulos</h2>
                  </div>
                  <span className="module-count">2 áreas</span>
                </div>

                <div className="module-grid">
                  <button
                    type="button"
                    className="module-card"
                    onClick={() => setPagina("patients")}
                  >
                    <span
                      className="module-icon patients-icon"
                      aria-hidden="true"
                    >
                      ♙
                    </span>
                    <span className="module-title">Pacientes</span>
                    <span className="module-description">
                      Acesse a área de gerenciamento de pacientes.
                    </span>
                    <span className="module-action">
                      Abrir módulo <span aria-hidden="true">→</span>
                    </span>
                  </button>

                  <article className="module-card module-card-muted">
                    <span
                      className="module-icon overview-icon"
                      aria-hidden="true"
                    >
                      ◫
                    </span>
                    <span className="module-title">Visão geral</span>
                    <span className="module-description">
                      Ponto de partida para acompanhar sua operação.
                    </span>
                    <span className="module-action module-action-muted">
                      Módulo inicial
                    </span>
                  </article>
                </div>
              </section>

              <div className="dashboard-notice">
                <span className="notice-icon" aria-hidden="true">
                  i
                </span>
                <p>
                  O painel está em desenvolvimento. Novas funcionalidades serão
                  disponibilizadas conforme os módulos forem integrados.
                </p>
              </div>
            </div>
          ) : (
            <PatientsPage token={token} onUnauthorized={handleUnauthorized} />
          )}

          <footer className="app-footer">
            <span>© {CURRENT_YEAR} TAGOX Tech</span>
            <span>TAGOX Flow · Gestão conectada</span>
          </footer>
        </section>
      </main>
    );
  }

  return (
    <main className="login-page">
      {" "}
      <section className="login-panel">
        {" "}
        <header className="brand">
          {" "}
          <div className="brand-symbol" aria-hidden="true">
            T{" "}
          </div>
          <div className="brand-text">
            <span className="brand-name">
              TAGOX<span>FLOW</span>
            </span>
            <span className="brand-caption">GESTÃO INTELIGENTE</span>
          </div>
        </header>

        <div className="login-content">
          <div className="welcome">
            <span className="eyebrow">SEU TRABALHO, CONECTADO</span>
            <h1>Bem-vindo de volta.</h1>
            <p>
              Entre na sua conta para acessar o ambiente de gestão da sua
              empresa.
            </p>
          </div>

          <form className="login-form" onSubmit={handleLogin}>
            <div className="field">
              <label htmlFor="tenantSlug">Empresa</label>
              <input
                id="tenantSlug"
                name="tenantSlug"
                type="text"
                placeholder="Identificador da empresa"
                value={tenantSlug}
                onChange={(event) => setTenantSlug(event.target.value)}
                autoComplete="organization"
                required
                disabled={carregando}
              />
            </div>

            <div className="field">
              <label htmlFor="email">E-mail</label>
              <input
                id="email"
                name="email"
                type="email"
                placeholder="voce@empresa.com"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                autoComplete="username"
                required
                disabled={carregando}
              />
            </div>

            <div className="field">
              <div className="label-row">
                <label htmlFor="senha">Senha</label>
              </div>

              <div className="password-input">
                <input
                  id="senha"
                  name="senha"
                  type={mostrarSenha ? "text" : "password"}
                  placeholder="Digite sua senha"
                  value={senha}
                  onChange={(event) => setSenha(event.target.value)}
                  autoComplete="current-password"
                  required
                  disabled={carregando}
                />

                <button
                  className="toggle-password"
                  type="button"
                  onClick={() => setMostrarSenha(!mostrarSenha)}
                  aria-label={mostrarSenha ? "Ocultar senha" : "Mostrar senha"}
                  disabled={carregando}
                >
                  {mostrarSenha ? "Ocultar" : "Mostrar"}
                </button>
              </div>
            </div>

            {erro && (
              <div className="error-message" role="alert">
                {erro}
              </div>
            )}

            <button
              className="login-button"
              type="submit"
              disabled={carregando}
            >
              {carregando ? (
                <>
                  <span className="spinner" aria-hidden="true" />
                  Entrando...
                </>
              ) : (
                <>
                  Entrar na plataforma
                  <span aria-hidden="true">→</span>
                </>
              )}
            </button>
          </form>

          <div className="security-note">
            <span className="security-icon" aria-hidden="true">
              ✓
            </span>
            <span>Seu acesso é protegido e individual.</span>
          </div>
        </div>

        <footer className="login-footer">
          <span>© {CURRENT_YEAR} TAGOX Tech</span>
          <span>TAGOX Flow · Gestão conectada</span>
        </footer>
      </section>

      <aside className="visual-panel" aria-label="Apresentação do TAGOX Flow">
        <div className="visual-grid" aria-hidden="true" />

        <div className="visual-content">
          <div className="visual-badge">
            <span className="status-dot" />
            UM NOVO FLUXO DE TRABALHO
          </div>

          <h2>
            Menos processos
            <br />
            desconectados.
            <br />
            <span>Mais possibilidades.</span>
          </h2>

          <p>
            Uma plataforma para conectar pessoas, organizar operações e
            simplificar a gestão dos serviços profissionais.
          </p>

          <div className="visual-line" />

          <div className="visual-signature">
            <span className="signature-mark">T.</span>
            <span>TECNOLOGIA QUE CONECTA.</span>
          </div>
        </div>

        <div className="visual-decoration decoration-one" aria-hidden="true" />
        <div className="visual-decoration decoration-two" aria-hidden="true" />
      </aside>
    </main>
  );
}

export default App;
