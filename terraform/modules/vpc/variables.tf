variable "vpc_cidr" { default = "10.0.0.0/16" }
variable "public_subnet_cidr" { default = "10.0.1.0/24" }
variable "environment" { default = "prod" }
variable "availability_zone" { default = "us-east-2a" }