export type Patient = {
  id: string
  nome: string
  cpf: string | null
  dataNascimento: string | null
  telefone: string | null
  email: string | null
}

export type PatientForm = {
  nome: string
  cpf: string
  dataNascimento: string
  telefone: string
  email: string
}

export type PatientsPageProps = {
  token: string
  onUnauthorized: () => void
}
