output "host_url" {
  value       = "https://${module.compute.instance_public_ip}/DevOps"
  description = "URL base objetivo solicitada para la ejecución del comando cURL del reto"
}

output "k3s_server_ip" {
  value = module.compute.instance_public_ip
}