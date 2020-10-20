<template>
  <v-row>
    <v-col sm="8" offset-sm="2">
    <v-data-table
      :headers="headers"
      :items="orders"
      sort-by="Name"
      class="elevation-1"
    >
      <template v-slot:top>
        <v-toolbar flat color="white">
          <v-toolbar-title>Orders Table</v-toolbar-title>
          <v-divider class="mx-4" inset vertical></v-divider>
          <v-spacer></v-spacer>
        </v-toolbar>
      </template>
    </v-data-table>
    </v-col>
  </v-row>
 </template>

<script>
import axios from "axios";
export default {
  name: "Orders",

  data: () => ({
    stores: [],
    headers: [
      { text: "Order", value: "orderID", align: "start", sortable: true },
      { text: "Customer", value: "customerID", sortable: true },
      { text: "Product", value: "productID", sortable: true }
    ],
  }),
  created () {
    this.initialize()
  },
  methods: {
    initialize () {
      axios.get("/orders/").then((resp) => (this.orders = resp.data));
    }
  }
};
</script>
