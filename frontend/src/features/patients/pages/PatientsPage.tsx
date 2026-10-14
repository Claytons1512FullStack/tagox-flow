import { useCallback, useEffect, useState, type FormEvent } from 'react'
import type {
  Patient,
  PatientForm,
  PatientsPageProps,
} from '../types/patient'
import {
  interpretarListaPacientes,
  listarPacientes,
  readError,
  salvarPacienteNaApi,
} from '../services/patientService'

const emptyForm: PatientForm = {
  nome: '',
  cpf: '',
  dataNascimento: '',
  telefone: '',
  email: '',
}

function maskCpf(cpf: string | null): string {
  if (!cpf) return '—'

  const digits = cpf.replace(/\D/g, '')

  if (digits.length !== 11) return cpf

  return `***.***.${digits.slice(6, 9)}-${digits.slice(9)}`
}

function PatientsPage({ token, onUnauthorized }: PatientsPageProps) {
  const [patients, setPatients] = useState<Patient[]>([])
  const [busca, setBusca] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [formAberto, setFormAberto] = useState(false)
  const [pacienteEditando, setPacienteEditando] = useState<string | null>(null)
  const [form, setForm] = useState<PatientForm>(emptyForm)

  const carregarPacientes = useCallback(async () => {
    setCarregando(true)
    setErro('')

    try {
      const response = await listarPacientes(token)

      if (response.status === 401) {
        onUnauthorized()
        return
      }

      const data = await interpretarListaPacientes(response)
      setPatients(data)
    } catch (error) {
      setErro(
        error instanceof TypeError
          ? 'Não foi possível conectar ao servidor. Confira se o backend está funcionando.'
          : error instanceof Error
            ? error.message
            : 'Ocorreu um erro inesperado ao carregar os pacientes.',
      )
    } finally {
      setCarregando(false)
    }
  }, [token, onUnauthorized])

  useEffect(() => {
    void carregarPacientes()
  }, [carregarPacientes])

  const pacientesFiltrados = patients.filter((patient) => {
    const termo = busca.trim().toLowerCase()

    if (!termo) return true

    return [
      patient.nome,
      patient.cpf || '',
      patient.telefone || '',
      patient.email || '',
    ].some((valor) => valor.toLowerCase().includes(termo))
  })

  function abrirCadastro() {
    setPacienteEditando(null)
    setForm(emptyForm)
    setErro('')
    setSucesso('')
    setFormAberto(true)
  }

  function abrirEdicao(patient: Patient) {
    setPacienteEditando(patient.id)
    setForm({
      nome: patient.nome || '',
      cpf: patient.cpf || '',
      dataNascimento: patient.dataNascimento || '',
      telefone: patient.telefone || '',
      email: patient.email || '',
    })
    setErro('')
    setSucesso('')
    setFormAberto(true)
  }

  function fecharFormulario() {
    if (salvando) return

    setFormAberto(false)
    setPacienteEditando(null)
    setForm(emptyForm)
    setErro('')
  }

  async function salvarPaciente(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErro('')
    setSucesso('')

    const payload = {
      nome: form.nome.trim(),
      cpf: form.cpf.trim() || null,
      dataNascimento: form.dataNascimento || null,
      telefone: form.telefone.trim() || null,
      email: form.email.trim() || null,
    }

    setSalvando(true)

    try {
      const editando = pacienteEditando !== null

      const response = await salvarPacienteNaApi(
        token,
        pacienteEditando,
        payload,
      )

      if (response.status === 401) {
        onUnauthorized()
        return
      }

      if (!response.ok) {
        throw new Error(await readError(response))
      }

      setFormAberto(false)
      setPacienteEditando(null)
      setForm(emptyForm)
      setSucesso(
        editando
          ? 'Dados do paciente atualizados com sucesso.'
          : 'Paciente cadastrado com sucesso.',
      )

      await carregarPacientes()
    } catch (error) {
      setErro(
        error instanceof TypeError
          ? 'Não foi possível conectar ao servidor. Tente novamente.'
          : error instanceof Error
            ? error.message
            : 'Ocorreu um erro inesperado ao salvar o paciente.',
      )
    } finally {
      setSalvando(false)
    }
  }

  return (
    <div className="dashboard-content patients-content">
      <section className="dashboard-welcome patients-welcome">
        <span className="eyebrow">GESTÃO DE ATENDIMENTOS</span>
        <h2>Seus pacientes, organizados.</h2>
        <p>
          Consulte os cadastros e mantenha as informações dos seus pacientes
          atualizadas em um só lugar.
        </p>
      </section>

      {erro && (
        <div className="patients-alert patients-alert-error" role="alert">
          <span>{erro}</span>
          {!formAberto && (
            <button
              type="button"
              onClick={() => void carregarPacientes()}
              disabled={carregando}
            >
              Tentar novamente
            </button>
          )}
        </div>
      )}

      {sucesso && (
        <div className="patients-alert patients-alert-success" role="status">
          {sucesso}
        </div>
      )}

      <section className="patients-panel">
        <div className="patients-toolbar">
          <div>
            <span className="topbar-eyebrow">CADASTROS</span>
            <h2>Lista de pacientes</h2>
            <p>
              {patients.length}{' '}
              {patients.length === 1
                ? 'paciente cadastrado'
                : 'pacientes cadastrados'}
            </p>
          </div>

          <button
            type="button"
            className="patients-primary-button"
            onClick={abrirCadastro}
          >
            <span aria-hidden="true">＋</span>
            Novo paciente
          </button>
        </div>

        <div className="patients-search">
          <label htmlFor="buscarPaciente">Buscar paciente</label>
          <div className="patients-search-input">
            <span aria-hidden="true">⌕</span>
            <input
              id="buscarPaciente"
              type="search"
              placeholder="Nome, CPF, telefone ou e-mail"
              value={busca}
              onChange={(event) => setBusca(event.target.value)}
            />
          </div>
        </div>

        {formAberto && (
          <form className="patient-form" onSubmit={salvarPaciente}>
            <div className="patient-form-heading">
              <div>
                <span className="topbar-eyebrow">
                  {pacienteEditando
                    ? 'ATUALIZAÇÃO DE CADASTRO'
                    : 'NOVO CADASTRO'}
                </span>
                <h3>
                  {pacienteEditando
                    ? 'Editar paciente'
                    : 'Cadastrar paciente'}
                </h3>
              </div>

              <button
                type="button"
                className="patients-close-button"
                onClick={fecharFormulario}
                disabled={salvando}
                aria-label="Fechar formulário"
              >
                ×
              </button>
            </div>

            <div className="patient-form-grid">
              <div className="patient-field patient-field-wide">
                <label htmlFor="patientNome">Nome completo *</label>
                <input
                  id="patientNome"
                  type="text"
                  maxLength={150}
                  autoComplete="name"
                  value={form.nome}
                  onChange={(event) =>
                    setForm({ ...form, nome: event.target.value })
                  }
                  required
                  disabled={salvando}
                />
              </div>

              <div className="patient-field">
                <label htmlFor="patientCpf">CPF</label>
                <input
                  id="patientCpf"
                  type="text"
                  inputMode="numeric"
                  maxLength={14}
                  placeholder="000.000.000-00"
                  value={form.cpf}
                  onChange={(event) =>
                    setForm({ ...form, cpf: event.target.value })
                  }
                  disabled={salvando}
                />
              </div>

              <div className="patient-field">
                <label htmlFor="patientNascimento">
                  Data de nascimento
                </label>
                <input
                  id="patientNascimento"
                  type="date"
                  value={form.dataNascimento}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      dataNascimento: event.target.value,
                    })
                  }
                  disabled={salvando}
                />
              </div>

              <div className="patient-field">
                <label htmlFor="patientTelefone">Telefone</label>
                <input
                  id="patientTelefone"
                  type="tel"
                  maxLength={20}
                  autoComplete="tel"
                  placeholder="(00) 00000-0000"
                  value={form.telefone}
                  onChange={(event) =>
                    setForm({ ...form, telefone: event.target.value })
                  }
                  disabled={salvando}
                />
              </div>

              <div className="patient-field">
                <label htmlFor="patientEmail">E-mail</label>
                <input
                  id="patientEmail"
                  type="email"
                  maxLength={150}
                  autoComplete="email"
                  placeholder="paciente@exemplo.com"
                  value={form.email}
                  onChange={(event) =>
                    setForm({ ...form, email: event.target.value })
                  }
                  disabled={salvando}
                />
              </div>
            </div>

            <p className="patient-form-note">
              Os campos marcados com * são obrigatórios. O CPF, quando
              informado, será validado pelo sistema.
            </p>

            <div className="patient-form-actions">
              <button
                type="button"
                className="patients-secondary-button"
                onClick={fecharFormulario}
                disabled={salvando}
              >
                Cancelar
              </button>

              <button
                type="submit"
                className="patients-primary-button"
                disabled={salvando}
              >
                {salvando
                  ? 'Salvando...'
                  : pacienteEditando
                    ? 'Salvar alterações'
                    : 'Cadastrar paciente'}
              </button>
            </div>
          </form>
        )}

        {carregando ? (
          <div className="patients-empty-state">
            <span
              className="patients-loading-spinner"
              aria-hidden="true"
            />
            <p>Carregando pacientes...</p>
          </div>
        ) : patients.length === 0 ? (
          <div className="patients-empty-state">
            <span className="patients-empty-icon" aria-hidden="true">
              ♙
            </span>
            <h3>Nenhum paciente cadastrado</h3>
            <p>
              Quando houver pacientes cadastrados, eles aparecerão nesta lista.
            </p>
            <button
              type="button"
              className="patients-secondary-button"
              onClick={abrirCadastro}
            >
              Cadastrar primeiro paciente
            </button>
          </div>
        ) : pacientesFiltrados.length === 0 ? (
          <div className="patients-empty-state">
            <h3>Nenhum resultado encontrado</h3>
            <p>Tente pesquisar com outro nome ou dado do paciente.</p>
            <button
              type="button"
              className="patients-secondary-button"
              onClick={() => setBusca('')}
            >
              Limpar busca
            </button>
          </div>
        ) : (
          <div className="patients-table-wrapper">
            <table className="patients-table">
              <thead>
                <tr>
                  <th>Paciente</th>
                  <th>CPF</th>
                  <th>Telefone</th>
                  <th>E-mail</th>
                  <th>
                    <span className="visually-hidden">Ações</span>
                  </th>
                </tr>
              </thead>

              <tbody>
                {pacientesFiltrados.map((patient) => (
                  <tr key={patient.id}>
                    <td>
                      <div className="patient-name-cell">
                        <span
                          className="patient-avatar"
                          aria-hidden="true"
                        >
                          {patient.nome.trim().charAt(0).toUpperCase()}
                        </span>
                        <strong>{patient.nome}</strong>
                      </div>
                    </td>

                    <td>{maskCpf(patient.cpf)}</td>
                    <td>{patient.telefone || '—'}</td>
                    <td>{patient.email || '—'}</td>

                    <td className="patient-actions-cell">
                      <button
                        type="button"
                        className="patient-edit-button"
                        onClick={() => abrirEdicao(patient)}
                        aria-label={`Editar cadastro de ${patient.nome}`}
                      >
                        Editar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {!carregando && pacientesFiltrados.length > 0 && (
          <div className="patients-table-footer">
            Exibindo {pacientesFiltrados.length} de {patients.length}{' '}
            {patients.length === 1 ? 'paciente' : 'pacientes'}
          </div>
        )}
      </section>
    </div>
  )
}

export default PatientsPage