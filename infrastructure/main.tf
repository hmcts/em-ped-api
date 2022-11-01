provider "azurerm" {
  features {}
}

locals {
  app_full_name = "${var.product}-${var.component}"
}
resource "azurerm_resource_group" "rg" {
  name     = "${local.app_full_name}-${var.env}"
  location = var.location
  tags     = var.common_tags
}

module "key_vault" {
  source                      = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  product                     = local.app_full_name
  env                         = var.env
  tenant_id                   = var.tenant_id
  object_id                   = var.jenkins_AAD_objectId
  resource_group_name         = "${local.app_full_name}-${var.env}"
  product_group_object_id     = "5d9cd025-a293-4b97-a0e5-6f43efce02c0"
  common_tags                 = var.common_tags
  managed_identity_object_ids = [data.azurerm_user_assigned_identity.rpa-shared-identity.principal_id]
}

data "azurerm_user_assigned_identity" "rpa-shared-identity" {
  name                = "rpa-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

resource "azurerm_web_pubsub" "ped_web_pubsub" {
  name                          = "${local.app_full_name}-webpubsub-${var.env}"
  location                      = var.location
  resource_group_name           = "${local.app_full_name}-${var.env}"
  sku                           = "Standard_S1"
  capacity                      = 1
  public_network_access_enabled = false
  live_trace {
    enabled                     = true
    messaging_logs_enabled      = true
    connectivity_logs_enabled   = false
  }
  tags                          = var.common_tags
  
  identity {
   type         = "UserAssigned"
   identity_ids = [data.azurerm_user_assigned_identity.rpa-shared-identity.id]
  }
}
