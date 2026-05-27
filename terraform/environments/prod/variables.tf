variable "aws_region" {
  type        = string
  default     = "us-east-2"
  description = "Región de despliegue en AWS"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "ami_id" {
  type    = string
  default = "ami-06e3c045d79fd65d9" # Ubuntu 22.04 LTS o tu AMI testeada
}

variable "instance_type" {
  type    = string
  default = "c7i-flex.large" # Excelente balance rendimiento/precio para la suite completa
}

variable "key_name" {
  type        = string
  default     = "devopsr3-key"
  description = "Nombre de la llave EC2 SSH registrada en tu consola de AWS"
}